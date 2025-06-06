package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArrayIndexesNode;
import ru.nest.hiscript.pol.model.BooleanNode;
import ru.nest.hiscript.pol.model.ExpressionNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.PrefixNode;
import ru.nest.hiscript.pol.model.TriggerNode;
import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class ExpressionParseRule extends ParseRule<ExpressionNode> {
	private final static ExpressionParseRule instance = new ExpressionParseRule();

	public static ExpressionParseRule getInstance() {
		return instance;
	}

	private ExpressionParseRule() {
	}

	@Override
	public ExpressionNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		PrefixNode prefix = PrefixParseRule.getInstance().visit(tokenizer);
		Node value = visitSimpleExpression(tokenizer);
		if (value == null && prefix != null) {
			throw new HiScriptParseException("expression expected", tokenizer.currentToken());
		}

		if (value != null) {
			ArrayIndexesNode index = ArrayIndexesParseRule.getInstance().visit(tokenizer);
			ExpressionNode node = new ExpressionNode(prefix, value, index);

			SymbolType operation;
			while ((operation = visitOperation(tokenizer)) != null) {
				prefix = PrefixParseRule.getInstance().visit(tokenizer);
				value = visitSimpleExpression(tokenizer);
				if (value == null) {
					throw new HiScriptParseException("illegal start of expression", tokenizer.currentToken());
				}
				index = ArrayIndexesParseRule.getInstance().visit(tokenizer);
				node.doOperation(operation, prefix, value, index);
			}

			// visit trigger
			if (visitSymbol(tokenizer, SymbolType.QUESTION) != null) {
				Node trueValue = visit(tokenizer);
				if (trueValue == null) {
					throw new HiScriptParseException("expression expected", tokenizer.currentToken());
				}

				expectSymbol(SymbolType.COLON, tokenizer);

				Node falseValue = visit(tokenizer);
				if (falseValue == null) {
					throw new HiScriptParseException("expression expected", tokenizer.currentToken());
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
			errorOccurred(tokenizer, handler, "expression expected");
		}

		if (value) {
			ArrayIndexesParseRule.getInstance().visit(tokenizer, handler);

			while (visitOperation(tokenizer, handler) != null) {
				prefix = PrefixParseRule.getInstance().visit(tokenizer, handler);
				value = visitSimpleExpression(tokenizer, handler);
				if (!value) {
					errorOccurred(tokenizer, handler, "illegal start of expression");
				}
				ArrayIndexesParseRule.getInstance().visit(tokenizer, handler);
			}

			// visit trigger
			if (visitSymbol(tokenizer, handler, SymbolType.QUESTION) != null) {
				boolean trueValue = visit(tokenizer, handler);
				if (!trueValue) {
					errorOccurred(tokenizer, handler, "expression expected");
				}

				expectSymbol(SymbolType.COLON, tokenizer, handler);

				boolean falseValue = visit(tokenizer, handler);
				if (!falseValue) {
					errorOccurred(tokenizer, handler, "expression expected");
				}
			}
			return true;
		}
		return false;
	}

	protected Node visitSimpleExpression(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		// visit number
		Node node;
		if ((node = visitNumber(tokenizer)) != null) {
			return node;
		}

		// visit boolean
		WordType boolType = visitWords(tokenizer, WordType.TRUE, WordType.FALSE);
		if (boolType != null) {
			return new BooleanNode(boolType == WordType.TRUE);
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
		if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
			ExpressionNode enode = ExpressionParseRule.getInstance().visit(tokenizer);
			if (enode == null) {
				throw new HiScriptParseException("expression expected", tokenizer.currentToken());
			}

			if (enode.getValues().size() == 1) {
				node = enode.getValues().get(0);
			} else {
				node = enode;
			}

			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);
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
		WordType boolType = visitWords(tokenizer, handler, WordType.TRUE, WordType.FALSE);
		if (boolType != null) {
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
		if (visitSymbol(tokenizer, handler, SymbolType.PARENTHESES_LEFT) != null) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}

			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);
			return true;
		}
		return false;
	}

	private SymbolType visitOperation(Tokenizer tokenizer) throws TokenizerException {
		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (OperationSymbols.isOperation(symbolToken.getType())) {
				tokenizer.nextToken();
				return symbolToken.getType();
			}
		}
		return null;
	}

	private SymbolType visitOperation(Tokenizer tokenizer, CompileHandler handler) {
		try {
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				if (OperationSymbols.isOperation(symbolToken.getType())) {
					tokenizer.nextToken();
					return symbolToken.getType();
				}
			}
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return null;
	}
}
