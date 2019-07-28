package script.pol;

import script.ParseException;
import script.pol.model.ArrayIndexesNode;
import script.pol.model.BooleanNode;
import script.pol.model.ExpressionNode;
import script.pol.model.Node;
import script.pol.model.Operations;
import script.pol.model.PrefixNode;
import script.pol.model.TriggerNode;
import script.tokenizer.OperationSymbols;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Token;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class ExpressionParseRule extends ParseRule<ExpressionNode> {
	private final static ExpressionParseRule instance = new ExpressionParseRule();

	public static ExpressionParseRule getInstance() {
		return instance;
	}

	private ExpressionParseRule() {
	}

	@Override
	public ExpressionNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		PrefixNode prefix = PrefixParseRule.getInstance().visit(tokenizer);
		Node value = visitSimpleExpression(tokenizer);
		if (value == null && prefix != null) {
			throw new ParseException("expression is expected", tokenizer.currentToken());
		}

		if (value != null) {
			ArrayIndexesNode index = ArrayIndexesParseRule.getInstance().visit(tokenizer);
			ExpressionNode node = new ExpressionNode(prefix, value, index);

			int operation;
			while ((operation = visitOperation(tokenizer)) != -1) {
				prefix = PrefixParseRule.getInstance().visit(tokenizer);
				value = visitSimpleExpression(tokenizer);
				if (value == null) {
					throw new ParseException("illegal start of expression", tokenizer.currentToken());
				}
				index = ArrayIndexesParseRule.getInstance().visit(tokenizer);
				node.doOperation(operation, prefix, value, index);
			}

			// visit trigger
			if (visitSymbol(tokenizer, Symbols.QUESTION) != -1) {
				Node trueValue = visit(tokenizer);
				if (trueValue == null) {
					throw new ParseException("expression is expected", tokenizer.currentToken());
				}

				expectSymbol(Symbols.COLON, tokenizer);

				Node falseValue = visit(tokenizer);
				if (falseValue == null) {
					throw new ParseException("expression is expected", tokenizer.currentToken());
				}

				TriggerNode trigger = new TriggerNode(node, trueValue, falseValue);
				node = new ExpressionNode(null, trigger, null);
			}

			return node;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		boolean prefix = PrefixParseRule.getInstance().visit(tokenizer, handler);
		boolean value = visitSimpleExpression(tokenizer, handler);
		if (!value && prefix) {
			errorOccured(tokenizer, handler, "expression is expected");
		}

		if (value) {
			ArrayIndexesParseRule.getInstance().visit(tokenizer, handler);

			while (visitOperation(tokenizer, handler) != -1) {
				prefix = PrefixParseRule.getInstance().visit(tokenizer, handler);
				value = visitSimpleExpression(tokenizer, handler);
				if (!value) {
					errorOccured(tokenizer, handler, "illegal start of expression");
				}
				ArrayIndexesParseRule.getInstance().visit(tokenizer, handler);
			}

			// visit trigger
			if (visitSymbol(tokenizer, handler, Symbols.QUESTION) != -1) {
				boolean trueValue = visit(tokenizer, handler);
				if (!trueValue) {
					errorOccured(tokenizer, handler, "expression is expected");
				}

				expectSymbol(Symbols.COLON, tokenizer, handler);

				boolean falseValue = visit(tokenizer, handler);
				if (!falseValue) {
					errorOccured(tokenizer, handler, "expression is expected");
				}
			}

			return true;
		}

		return false;
	}

	protected Node visitSimpleExpression(Tokenizer tokenizer) throws TokenizerException, ParseException {
		// visit number
		Node node;
		if ((node = visitNumber(tokenizer)) != null) {
			return node;
		}

		// visit boolean
		int boolType = visitWords(tokenizer, Words.TRUE, Words.FALSE);
		if (boolType != -1) {
			return new BooleanNode(boolType == Words.TRUE);
		}

		// visit method invocation
		if ((node = InvocationParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		// visit increment
		if ((node = IncrementParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		// visit variable as word
		if ((node = visitVariable(tokenizer)) != null) {
			return node;
		}

		// visit character
		if ((node = visitCharacter(tokenizer)) != null) {
			return node;
		}

		// visit string
		if ((node = visitString(tokenizer)) != null) {
			return node;
		}

		// visit array
		if ((node = ArrayParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		// visit block
		if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
			ExpressionNode enode = ExpressionParseRule.getInstance().visit(tokenizer);
			if (enode == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}

			if (enode.getValues().size() == 1) {
				node = enode.getValues().get(0);
			} else {
				node = enode;
			}

			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);
			return node;
		}

		return null;
	}

	protected boolean visitSimpleExpression(Tokenizer tokenizer, CompileHandler handler) {
		// visit number
		if (visitNumber(tokenizer, handler)) {
			return true;
		}

		// visit boolean
		int boolType = visitWords(tokenizer, handler, Words.TRUE, Words.FALSE);
		if (boolType != -1) {
			return true;
		}

		// visit method invocation
		if (InvocationParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		// visit increment
		if (IncrementParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		// visit variable as word
		if (visitVariable(tokenizer, handler) != null) {
			return true;
		}

		// visit character
		if (visitCharacter(tokenizer, handler)) {
			return true;
		}

		// visit string
		if (visitString(tokenizer, handler)) {
			return true;
		}

		// visit array
		if (ArrayParseRule.getInstance().visit(tokenizer, handler)) {
			return true;
		}

		// visit block
		if (visitSymbol(tokenizer, handler, Symbols.PARANTHESIS_LEFT) != -1) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}

			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);
			return true;
		}

		return false;
	}

	private int visitOperation(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (OperationSymbols.isOperation(symbolToken.getType())) {
				tokenizer.nextToken();
				return symbolToken.getType();
			}
		}

		return -1;
	}

	private int visitOperation(Tokenizer tokenizer, CompileHandler handler) {
		try {
			skipComments(tokenizer);

			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				if (OperationSymbols.isOperation(symbolToken.getType())) {
					tokenizer.nextToken();
					return symbolToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return -1;
	}
}
