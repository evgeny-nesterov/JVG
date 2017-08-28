package script.ool.compiler;

import java.util.ArrayList;
import java.util.List;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.Operation;
import script.ool.model.Operations;
import script.ool.model.OperationsGroup;
import script.ool.model.Type;
import script.ool.model.nodes.NodeBoolean;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeIdentificator;
import script.ool.model.nodes.NodeInvocation;
import script.ool.model.nodes.NodeNull;
import script.ool.model.nodes.NodeSuper;
import script.ool.model.nodes.NodeThis;
import script.ool.model.nodes.NodeType;
import script.tokenizer.OperationSymbols;
import script.tokenizer.StringToken;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Token;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ExpressionParseRule extends ParseRule<NodeExpression> {
	private final static ExpressionParseRule instance = new ExpressionParseRule();

	public static ExpressionParseRule getInstance() {
		return instance;
	}

	private ExpressionParseRule() {
	}

	public NodeExpression visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		List<Node> operands = new ArrayList<Node>();
		List<OperationsGroup> allOperations = new ArrayList<OperationsGroup>();

		Token token = tokenizer.currentToken();
		int codeLine = token != null ? token.getLine() : -1;
		int skipCount = 0;

		int operation = -1;
		OperationsGroup operations = new OperationsGroup();
		do {
			visitPrefixes(tokenizer, operations, operands);
			visitIncrement(tokenizer, operations, true);
			allOperations.add(operations);

			operations = new OperationsGroup();
			visitSimpleExpression(tokenizer, operations, allOperations, operands, properties);
			visitArrayIndexes(tokenizer, operations, operands, properties);
			visitIncrement(tokenizer, operations, false);

			tokenizer.start();
			operation = visitOperation(tokenizer);
			if (operation != -1) {
				if (operation == Operations.SKIP) {
					skipCount++;
				} else if (operation == Operations.TRIGER) {
					if (skipCount == 0) {
						tokenizer.rollback();
						break;
					}
					skipCount--;
				} else if (operation == Operations.LOGICAL_AND) {
					operations.addPostfixOperation(Operations.LOGICAL_AND_CHECK);
				} else if (operation == Operations.LOGICAL_OR) {
					operations.addPostfixOperation(Operations.LOGICAL_OR_CHECK);
				}

				tokenizer.commit();
				operations.setOperation(operation);
			}
		} while (operation != -1);

		allOperations.add(operations);

		// DEBUG
		// System.out.println(allOperations);

		if (operands.size() > 0) {
			Node[] operandsArray = new Node[operands.size()];
			operands.toArray(operandsArray);

			OperationsGroup[] operationsArray = new OperationsGroup[allOperations.size()];
			allOperations.toArray(operationsArray);

			NodeExpression expressionNode = new NodeExpression(operandsArray, operationsArray, codeLine);
			return expressionNode;
		}

		if (operations.hasOperations()) {
			throw new ParseException("invalid expression", token);
		}

		return null;
	}

	public boolean visitPrefixes(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands) throws TokenizerException, ParseException {
		boolean found = false;
		while (true) {
			int operation = visitSymbol(tokenizer, SymbolToken.PLUS, SymbolToken.MINUS, SymbolToken.EXCLAMATION);
			if (operation != -1) {
				switch (operation) {
					case SymbolToken.PLUS:
						operation = Operations.PREFIX_PLUS;
						break;

					case SymbolToken.MINUS:
						operation = Operations.PREFIX_MINUS;
						break;

					case SymbolToken.EXCLAMATION:
						operation = Operations.PREFIX_EXCLAMATION;
						break;
				}

				found = true;
				operations.addPrefixOperation(operation);
				continue;
			}

			if (visitCast(tokenizer, operations, operands)) {
				found = true;
				continue;
			}
			break;
		}
		return found;
	}

	public boolean visitIncrement(Tokenizer tokenizer, OperationsGroup operations, boolean prefix) throws TokenizerException, ParseException {
		int operation = visitSymbol(tokenizer, SymbolToken.PLUS_PLUS, SymbolToken.MINUS_MINUS);
		if (operation != -1) {
			if (operation == SymbolToken.PLUS_PLUS) {
				operation = prefix ? Operations.PREFIX_INCREMENT : Operations.POST_INCREMENT;
			} else {
				operation = prefix ? Operations.PREFIX_DECREMENT : Operations.POST_DECREMENT;
			}

			if (prefix) {
				operations.addPrefixOperation(operation);
			} else {
				operations.addPostfixOperation(operation);
			}
			return true;
		}

		return false;
	}

	public boolean visitCast(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands) throws TokenizerException, ParseException {
		tokenizer.start();
		if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT) != -1) {
			Type type = visitType(tokenizer, true);
			if (type != null) {
				if (type.getDimension() == 0) {
					// (a.b.c) (type)(var) + 1 - ???
					if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_RIGHT) != -1) {
						if (type.isPrimitive()) {
							// for cases (int), (float) an so on
							tokenizer.commit();

							Node typeNode = new NodeType(type);
							operands.add(typeNode);
							operations.addPrefixOperation(Operations.CAST);
							return true;
						} else {
							// for case (A) it's not known whether A is a type or an variable
							// it's needs to check on (A)<value without prefixes for primitives>

							boolean isCast = false;
							Token currentToken = tokenizer.currentToken();
							if (currentToken instanceof WordToken || currentToken instanceof StringToken) {
								isCast = true;
							} else if (checkSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
								isCast = true;
							}

							if (isCast) {
								tokenizer.commit();

								Node typeNode = new NodeType(type);
								operands.add(typeNode);
								operations.addPrefixOperation(Operations.CAST);
								return true;
							}
						}
					}
				} else {
					// for cases (int[]), (A.B.C[][]), (A[]) and so on
					tokenizer.commit();
					expectSymbol(tokenizer, SymbolToken.PARANTHESIS_RIGHT);

					Node typeNode = new NodeType(type);
					operands.add(typeNode);
					operations.addPrefixOperation(Operations.CAST);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}

	public boolean visitArrayIndex(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands, CompileContext properties) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, SymbolToken.SQUARE_BRACES_LEFT) != -1) {
			Token token = tokenizer.currentToken();

			Node indexNode = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			if (indexNode == null) {
				throw new ParseException("expression is expected", token);
			}

			expectSymbol(tokenizer, SymbolToken.SQUARE_BRACES_RIGHT);

			operands.add(indexNode);
			operations.addPostfixOperation(Operations.ARRAY_INDEX);
			return true;
		}

		return false;
	}

	public boolean visitArrayIndexes(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands, CompileContext properties) throws TokenizerException, ParseException {
		boolean found = false;
		while (visitArrayIndex(tokenizer, operations, operands, properties)) {
			found = true;
		}
		return found;
	}

	protected boolean visitSimpleExpression(Tokenizer tokenizer, OperationsGroup operations, List<OperationsGroup> allOperations, List<Node> operands, CompileContext properties) throws TokenizerException, ParseException {
		// visit number
		Node node;
		if ((node = visitNumber(tokenizer)) != null) {
			operands.add(node);
			return true;
		}

		// visit boolean
		int boolType = visitWordType(tokenizer, WordToken.TRUE, WordToken.FALSE);
		if (boolType != -1) {
			operands.add(NodeBoolean.getInstance(boolType == WordToken.TRUE));
			return true;
		}

		// visit null
		if (visitWordType(tokenizer, WordToken.NULL) != -1) {
			operands.add(NodeNull.instance);
			return true;
		}

		// visit this
		if (visitWordType(tokenizer, WordToken.THIS) != -1) {
			operands.add(NodeThis.instance);
			return true;
		}

		// visit super
		if (visitWordType(tokenizer, WordToken.SUPER) != -1) {
			operands.add(NodeSuper.instance);
			return true;
		}

		// visit character
		if ((node = visitCharacter(tokenizer)) != null) {
			operands.add(node);
			return true;
		}

		// visit string
		if ((node = visitString(tokenizer)) != null) {
			operands.add(node);
			return true;
		}

		// visit new object or array
		if ((node = NewParseRule.getInstance().visit(tokenizer, properties)) != null) {
			operands.add(node);
			return true;
		}

		// visit block
		if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT) != -1) {
			node = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			if (node == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(tokenizer, SymbolToken.PARANTHESIS_RIGHT);
			operands.add(node);
			return true;
		}

		// visit method name with arguments
		if ((node = InvocationParseRule.getInstance().visit(tokenizer, properties)) != null) {
			// determin is there prefix before method
			OperationsGroup lastOperationGroup = allOperations.size() > 0 ? allOperations.get(allOperations.size() - 1) : null;
			Operation lastOperation = lastOperationGroup != null ? lastOperationGroup.getOperation() : null;
			boolean inner = lastOperation == null || lastOperation.getOperation() != Operations.INVOCATION;

			((NodeInvocation) node).setInner(inner);
			operands.add(node);
			return true;
		}

		// visit identificator as word: package, class, method, field
		String identificatorName = visitWord(WordToken.NOT_SERVICE, tokenizer);
		if (identificatorName != null) {
			operands.add(new NodeIdentificator(identificatorName));
			return true;
		}

		return false;
	}

	private int visitOperation(Tokenizer tokenizer) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (OperationSymbols.isOperation(symbolToken.getType()) || symbolToken.getType() == Symbols.QUESTION || symbolToken.getType() == Symbols.COLON) {
				tokenizer.nextToken();
				return Operations.mapSymbolToOperation(symbolToken.getType());
			}
		} else if (currentToken instanceof WordToken) {
			WordToken word = (WordToken) currentToken;
			if (word.getType() == INSTANCEOF) {
				tokenizer.nextToken();
				return Operations.INSTANCEOF;
			}
		}

		return -1;
	}
}
