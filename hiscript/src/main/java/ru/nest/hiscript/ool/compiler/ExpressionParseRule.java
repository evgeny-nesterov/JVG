package ru.nest.hiscript.ool.compiler;

import java.util.ArrayList;
import java.util.List;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationsIF;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeBoolean;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeIdentificator;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.ool.model.nodes.NodeNull;
import ru.nest.hiscript.ool.model.nodes.NodeSuper;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.StringToken;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class ExpressionParseRule extends ParseRule<NodeExpression> {
	private final static ExpressionParseRule instance = new ExpressionParseRule();

	public static ExpressionParseRule getInstance() {
		return instance;
	}

	private ExpressionParseRule() {
	}

	@Override
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
				if (operation == OperationsIF.SKIP) {
					skipCount++;
				} else if (operation == OperationsIF.TRIGER) {
					if (skipCount == 0) {
						tokenizer.rollback();
						break;
					}
					skipCount--;
				} else if (operation == OperationsIF.LOGICAL_AND) {
					operations.addPostfixOperation(OperationsIF.LOGICAL_AND_CHECK);
				} else if (operation == OperationsIF.LOGICAL_OR) {
					operations.addPostfixOperation(OperationsIF.LOGICAL_OR_CHECK);
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
			int operation = visitSymbol(tokenizer, Symbols.PLUS, Symbols.MINUS, Symbols.EXCLAMATION);
			if (operation != -1) {
				switch (operation) {
					case Symbols.PLUS:
						operation = OperationsIF.PREFIX_PLUS;
						break;

					case Symbols.MINUS:
						operation = OperationsIF.PREFIX_MINUS;
						break;

					case Symbols.EXCLAMATION:
						operation = OperationsIF.PREFIX_EXCLAMATION;
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
		int operation = visitSymbol(tokenizer, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);
		if (operation != -1) {
			if (operation == Symbols.PLUS_PLUS) {
				operation = prefix ? OperationsIF.PREFIX_INCREMENT : OperationsIF.POST_INCREMENT;
			} else {
				operation = prefix ? OperationsIF.PREFIX_DECREMENT : OperationsIF.POST_DECREMENT;
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
		if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
			Type type = visitType(tokenizer, true);
			if (type != null) {
				if (type.getDimension() == 0) {
					// (a.b.c) (type)(var) + 1 - ???
					if (visitSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT) != -1) {
						if (type.isPrimitive()) {
							// for cases (int), (float) an so on
							tokenizer.commit();

							Node typeNode = new NodeType(type);
							operands.add(typeNode);
							operations.addPrefixOperation(OperationsIF.CAST);
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
								operations.addPrefixOperation(OperationsIF.CAST);
								return true;
							}
						}
					}
				} else {
					// for cases (int[]), (A.B.C[][]), (A[]) and so on
					tokenizer.commit();
					expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

					Node typeNode = new NodeType(type);
					operands.add(typeNode);
					operations.addPrefixOperation(OperationsIF.CAST);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}

	public boolean visitArrayIndex(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands, CompileContext properties) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			Token token = tokenizer.currentToken();

			Node indexNode = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			if (indexNode == null) {
				throw new ParseException("expression is expected", token);
			}

			expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);

			operands.add(indexNode);
			operations.addPostfixOperation(OperationsIF.ARRAY_INDEX);
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
		int boolType = visitWordType(tokenizer, Words.TRUE, Words.FALSE);
		if (boolType != -1) {
			operands.add(NodeBoolean.getInstance(boolType == Words.TRUE));
			return true;
		}

		// visit null
		if (visitWordType(tokenizer, Words.NULL) != -1) {
			operands.add(NodeNull.instance);
			return true;
		}

		// visit this
		if (visitWordType(tokenizer, Words.THIS) != -1) {
			operands.add(NodeThis.instance);
			return true;
		}

		// visit super
		if (visitWordType(tokenizer, Words.SUPER) != -1) {
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
		if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
			node = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			if (node == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);
			operands.add(node);
			return true;
		}

		// visit method name with arguments
		if ((node = InvocationParseRule.getInstance().visit(tokenizer, properties)) != null) {
			// determin is there prefix before method
			OperationsGroup lastOperationGroup = allOperations.size() > 0 ? allOperations.get(allOperations.size() - 1) : null;
			Operation lastOperation = lastOperationGroup != null ? lastOperationGroup.getOperation() : null;
			boolean inner = lastOperation == null || lastOperation.getOperation() != OperationsIF.INVOCATION;

			((NodeInvocation) node).setInner(inner);
			operands.add(node);
			return true;
		}

		// visit identificator as word: package, class, method, field
		String identificatorName = visitWord(Words.NOT_SERVICE, tokenizer);
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
				return OperationsIF.INSTANCEOF;
			}
		}

		return -1;
	}
}
