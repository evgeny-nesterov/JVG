package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationsIF;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeBoolean;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.ool.model.nodes.NodeLogicalSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeNull;
import ru.nest.hiscript.ool.model.nodes.NodeNumber;
import ru.nest.hiscript.ool.model.nodes.NodeSuper;
import ru.nest.hiscript.ool.model.nodes.NodeThis;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.NumberToken;
import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.StringToken;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParseRule extends ParseRule<NodeExpression> {
	private final static ExpressionParseRule instance = new ExpressionParseRule();

	public static ExpressionParseRule getInstance() {
		return instance;
	}

	private ExpressionParseRule() {
	}

	@Override
	public NodeExpression visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		List<Node> operands = new ArrayList<>();
		List<OperationsGroup> allOperations = new ArrayList<>();
		Token startToken = startToken(tokenizer);

		int operation;
		OperationsGroup operations = new OperationsGroup();
		do {
			visitPrefixes(tokenizer, operations, operands);
			visitIncrement(tokenizer, operations, true);
			allOperations.add(operations);

			operations = new OperationsGroup();
			visitSimpleExpression(tokenizer, operations, allOperations, operands, ctx);
			visitArrayIndexes(tokenizer, operations, operands, ctx);
			visitIncrement(tokenizer, operations, false);

			// tokenizer.start();
			operation = visitOperation(tokenizer, operands);
			if (operation != -1) {
				// tokenizer.commit();
				if (operation == OperationsIF.LOGICAL_AND) {
					operations.addPostfixOperation(OperationsIF.LOGICAL_AND_CHECK);
				} else if (operation == OperationsIF.LOGICAL_OR) {
					operations.addPostfixOperation(OperationsIF.LOGICAL_OR_CHECK);
				}

				operations.setOperation(operation);
			} else {
				// tokenizer.rollback();
			}
		} while (operation != -1);

		allOperations.add(operations);

		// DEBUG
		// System.out.println(allOperations);

		Token blockToken = tokenizer.getBlockToken(startToken);
		if (operands.size() > 0) {
			Node[] operandsArray = new Node[operands.size()];
			operands.toArray(operandsArray);

			OperationsGroup[] operationsArray = new OperationsGroup[allOperations.size()];
			allOperations.toArray(operationsArray);

			NodeExpression expressionNode = new NodeExpressionNoLS(operandsArray, operationsArray);
			expressionNode.setToken(blockToken);
			if (visitSymbol(tokenizer, Symbols.QUESTION) != -1) {
				NodeExpression trueValueNode = visit(tokenizer, ctx);
				if (trueValueNode == null) {
					throw new ParseException("expression expected", tokenizer.currentToken());
				}

				expectSymbol(tokenizer, Symbols.COLON);

				NodeExpression falseValueNode = visit(tokenizer, ctx);
				if (falseValueNode == null) {
					throw new ParseException("expression expected", tokenizer.currentToken());
				}

				NodeLogicalSwitch logicalSwitchNode = new NodeLogicalSwitch(expressionNode, trueValueNode, falseValueNode);
				logicalSwitchNode.setToken(tokenizer.getBlockToken(startToken));
				return logicalSwitchNode;
			}
			return expressionNode;
		}

		if (operations.hasOperations()) {
			throw new ParseException("invalid expression", blockToken);
		}
		return null;
	}

	public boolean visitPrefixes(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands) throws TokenizerException, ParseException {
		boolean found = false;
		boolean isBoolean = false;
		boolean isNumber = false;
		tokenizer.start();
		while (true) {
			int operation = visitSymbol(tokenizer, Symbols.PLUS, Symbols.MINUS, Symbols.EXCLAMATION);
			if (operation != -1) {
				switch (operation) {
					case Symbols.PLUS:
						if (isBoolean) {
							tokenizer.rollback();
							return false;
						}
						operation = OperationsIF.PREFIX_PLUS;
						isNumber = true;
						break;

					case Symbols.MINUS:
						if (isBoolean) {
							tokenizer.rollback();
							return false;
						}
						operation = OperationsIF.PREFIX_MINUS;
						isNumber = true;
						break;

					case Symbols.EXCLAMATION:
						if (isNumber) {
							tokenizer.rollback();
							return false;
						}
						operation = OperationsIF.PREFIX_EXCLAMATION;
						isBoolean = true;
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
		tokenizer.commit();
		return found;
	}

	public boolean visitIncrement(Tokenizer tokenizer, OperationsGroup operations, boolean prefix) throws TokenizerException {
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
		if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
			Type type = visitType(tokenizer, true);
			if (type != null) {
				if (type.getDimension() == 0) {
					// (a.b.c) (type)(var) + 1 - ???
					if (visitSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) != -1) {
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
							} else if (checkSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
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
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

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

	public boolean visitArrayIndex(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands, CompileClassContext ctx) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			Token token = tokenizer.currentToken();

			Node indexNode = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
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

	public boolean visitArrayIndexes(Tokenizer tokenizer, OperationsGroup operations, List<Node> operands, CompileClassContext ctx) throws TokenizerException, ParseException {
		boolean found = false;
		while (visitArrayIndex(tokenizer, operations, operands, ctx)) {
			found = true;
		}
		return found;
	}

	protected boolean visitSimpleExpression(Tokenizer tokenizer, OperationsGroup operations, List<OperationsGroup> allOperations, List<Node> operands, CompileClassContext ctx) throws TokenizerException, ParseException {
		// visit number
		NodeNumber numberNode = visitNumber(tokenizer);
		if (numberNode != null) {
			operands.add(numberNode);
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
		Node node;
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
		if ((node = NewParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			operands.add(node);
			return true;
		}

		// visit switch
		if ((node = ExpressionSwitchParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			operands.add(node);
			return true;
		}

		// visit block
		if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
			node = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
			if (node == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
			operands.add(node);
			return true;
		}

		// visit method name with arguments
		if ((node = InvocationParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			// determine is there prefix before method
			OperationsGroup lastOperationGroup = allOperations.size() > 0 ? allOperations.get(allOperations.size() - 1) : null;
			Operation lastOperation = lastOperationGroup != null ? lastOperationGroup.getOperation() : null;
			boolean inner = lastOperation == null || lastOperation.getOperation() != OperationsIF.INVOCATION;

			((NodeInvocation) node).setInner(inner);
			operands.add(node);
			return true;
		}

		// visit identifier as word: package, class, method, field
		String identifierName = visitWord(tokenizer, Words.NOT_SERVICE);
		String primitiveTypeName = null;
		if (identifierName == null) {
			tokenizer.start();
			primitiveTypeName = visitWord(tokenizer, Words.BYTE, Words.SHORT, Words.INT, Words.LONG, Words.FLOAT, Words.DOUBLE, Words.BOOLEAN, Words.CHAR);
		}
		if (identifierName != null || primitiveTypeName != null) {
			Token identifierToken = tokenizer.currentToken();
			int dimension = visitDimension(tokenizer);
			if (primitiveTypeName != null) {
				if (dimension > 0) {
					tokenizer.commit();
					identifierName = primitiveTypeName;
				} else {
					tokenizer.rollback();
					return false;
				}
			}

			boolean visitCastAfterIdentifier = false;
			if (allOperations.size() > 0) {
				int index = allOperations.size() - 1;
				OperationsGroup lastOperation = allOperations.get(index);
				while (lastOperation.getOperation() != null && lastOperation.getOperation().getOperation() == Operations.INVOCATION) {
					lastOperation = allOperations.get(--index);
				}
				if (lastOperation == null || (lastOperation != null && lastOperation.getOperation() != null && lastOperation.getOperation().getOperation() == Operations.INSTANCE_OF) || !lastOperation.hasOperations()) {
					visitCastAfterIdentifier = true;
				}
			}

			NodeArgument[] castedRecordArguments = null;
			String castedVariableName = null;
			if (visitCastAfterIdentifier) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					List<NodeArgument> argumentsList = new ArrayList<>();
					visitArgumentsDefinitions(tokenizer, argumentsList, ctx);
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
					if (argumentsList.size() > 0) {
						castedRecordArguments = argumentsList.toArray(new NodeArgument[argumentsList.size()]);
					}
				}
				castedVariableName = visitWord(Words.NOT_SERVICE, tokenizer);
			}

			if (castedRecordArguments != null || castedVariableName != null) {
				NodeCastedIdentifier identifier = new NodeCastedIdentifier(identifierName, dimension);
				identifier.setToken(identifierToken);
				identifier.castedRecordArguments = castedRecordArguments;
				identifier.castedVariableName = castedVariableName;
				operands.add(identifier);
			} else {
				NodeIdentifier identifier = new NodeIdentifier(identifierName, dimension);
				operands.add(identifier);
			}
			return true;
		}
		return false;
	}

	private int visitOperation(Tokenizer tokenizer, List<Node> operands) throws TokenizerException {
		skipComments(tokenizer);

		Token currentToken = tokenizer.currentToken();
		if (currentToken instanceof SymbolToken) {
			SymbolToken symbolToken = (SymbolToken) currentToken;
			if (OperationSymbols.isOperation(symbolToken.getType())) {
				tokenizer.nextToken();
				return Operations.mapSymbolToOperation(symbolToken.getType());
			}
		} else if (currentToken instanceof WordToken) {
			WordToken word = (WordToken) currentToken;
			if (word.getType() == INSTANCE_OF) {
				tokenizer.nextToken();
				return OperationsIF.INSTANCE_OF;
			}
		} else if (currentToken instanceof NumberToken && operands.size() > 0 && ((NumberToken) currentToken).hasSign()) {
			return OperationsIF.PLUS;
		}
		return -1;
	}
}
