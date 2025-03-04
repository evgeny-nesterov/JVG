package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationType;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeBoolean;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeGetClass;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeInvocation;
import ru.nest.hiscript.ool.model.nodes.NodeLogicalSwitch;
import ru.nest.hiscript.ool.model.nodes.NodeMethodReference;
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

import java.util.ArrayList;
import java.util.List;

import static ru.nest.hiscript.tokenizer.Words.*;

public class ExpressionParseRule extends ParseRule<NodeExpression> {
	public final static ExpressionParseRule methodPriority = new ExpressionParseRule(Priority.method);

	public final static ExpressionParseRule castedIdentifierPriority = new ExpressionParseRule(Priority.castedIdentifier);

	enum Priority {
		method, castedIdentifier
	}

	private Priority priority;

	private ExpressionParseRule(Priority priority) {
		this.priority = priority;
	}

	@Override
	public NodeExpression visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		List<HiNodeIF> operands = new ArrayList<>(2);
		List<OperationsGroup> allOperations = new ArrayList<>(1);

		boolean valid = true;
		OperationType operation;
		OperationsGroup operations = new OperationsGroup();
		do {
			boolean expectOperand = visitPrefixes(tokenizer, ctx, operations, operands);
			expectOperand |= visitIncrement(tokenizer, operations, true);
			allOperations.add(operations);

			operations = new OperationsGroup();
			boolean hasOperand = visitSimpleExpression(tokenizer, allOperations, operands, ctx);
			expectOperand |= visitArrayIndexes(tokenizer, operations, operands, ctx);
			expectOperand |= visitIncrement(tokenizer, operations, false);

			operation = visitOperation(tokenizer, operands);
			if (operation != null) {
				expectOperand = true;

				if (operation == OperationType.LOGICAL_AND) {
					operations.addPostfixOperation(OperationType.LOGICAL_AND_CHECK);
				} else if (operation == OperationType.LOGICAL_OR) {
					operations.addPostfixOperation(OperationType.LOGICAL_OR_CHECK);
				}

				operations.setOperation(operation);
			}

			if (!hasOperand && expectOperand) {
				valid = false;
			}
		} while (operation != null);

		allOperations.add(operations);

		if (operands.size() > 0 && valid) {
			HiNodeIF[] operandsArray = operands.toArray(new HiNodeIF[operands.size()]);
			HiOperation[] operationsArray = NodeExpressionNoLS.compile(operandsArray, allOperations);
			NodeExpression expressionNode = new NodeExpressionNoLS(operandsArray, operationsArray);
			if (visitSymbol(tokenizer, Symbols.QUESTION) != -1) {
				NodeExpression trueValueNode = visit(tokenizer, ctx);
				if (trueValueNode == null) {
					tokenizer.error("expression expected");
				}

				expectSymbol(tokenizer, Symbols.COLON);

				NodeExpression falseValueNode = visit(tokenizer, ctx);
				if (falseValueNode == null) {
					tokenizer.error("expression expected");
				}
				return new NodeLogicalSwitch(expressionNode, trueValueNode, falseValueNode);
			} else if (visitSymbol(tokenizer, Symbols.DOUBLE_COLON) != -1) {
				String methodName = expectWords(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
				return new NodeMethodReference(expressionNode, methodName);
			}
			return expressionNode;
		}

		if (operations.hasOperations() || !valid) {
			tokenizer.error("invalid expression", tokenizer.getBlockToken(startToken));
		}
		return null;
	}

	public boolean visitPrefixes(Tokenizer tokenizer, CompileClassContext ctx, OperationsGroup operations, List<HiNodeIF> operands) throws TokenizerException, HiScriptParseException {
		boolean found = false;
		boolean isBoolean = false;
		boolean isNumber = false;
		tokenizer.start();
		while (true) {
			int operationSymbol = visitSymbol(tokenizer, Symbols.PLUS, Symbols.MINUS, Symbols.EXCLAMATION, Symbols.TILDA);
			if (operationSymbol != -1) {
				OperationType operationsType = null;
				switch (operationSymbol) {
					case Symbols.PLUS:
						if (isBoolean) {
							tokenizer.rollback();
							return false;
						}
						operationsType = OperationType.PREFIX_PLUS;
						isNumber = true;
						break;

					case Symbols.MINUS:
						if (isBoolean) {
							tokenizer.rollback();
							return false;
						}
						operationsType = OperationType.PREFIX_MINUS;
						isNumber = true;
						break;

					case Symbols.EXCLAMATION:
						if (isNumber) {
							tokenizer.rollback();
							return false;
						}
						operationsType = OperationType.PREFIX_EXCLAMATION;
						isBoolean = true;
						break;

					case Symbols.TILDA:
						if (isBoolean) {
							tokenizer.rollback();
							return false;
						}
						operationsType = OperationType.PREFIX_BITWISE_REVERSE;
						isNumber = true;
						break;
				}

				found = true;
				operations.addPrefixOperation(operationsType);
				continue;
			}

			if (visitCast(tokenizer, ctx, operations, operands)) {
				found = true;
				continue;
			}
			break;
		}
		tokenizer.commit();
		return found;
	}

	public boolean visitIncrement(Tokenizer tokenizer, OperationsGroup operations, boolean prefix) throws TokenizerException {
		int operationSymbol = visitSymbol(tokenizer, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);
		if (operationSymbol != -1) {
			OperationType operationsType;
			if (operationSymbol == Symbols.PLUS_PLUS) {
				operationsType = prefix ? OperationType.PREFIX_INCREMENT : OperationType.POST_INCREMENT;
			} else {
				operationsType = prefix ? OperationType.PREFIX_DECREMENT : OperationType.POST_DECREMENT;
			}

			if (prefix) {
				operations.addPrefixOperation(operationsType);
			} else {
				operations.addPostfixOperation(operationsType);
			}
			return true;
		}
		return false;
	}

	public boolean visitCast(Tokenizer tokenizer, CompileClassContext ctx, OperationsGroup operations, List<HiNodeIF> operands) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
			Type type = visitType(tokenizer, true, ctx.getEnv());
			if (type != null) {
				if (type.getDimension() == 0) {
					// (a.b.c) (type)(var) + 1 - ???
					if (visitSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) != -1) {
						if (type.isPrimitive()) {
							// for cases (int), (float) an so on
							tokenizer.commit();

							HiNode typeNode = new NodeType(type);
							operands.add(typeNode);
							operations.addPrefixOperation(OperationType.CAST);
							return true;
						} else {
							// for case (A) it's not known whether A is a type or a variable
							// it's needs to check on (A)<value without prefixes for primitives>

							boolean isCast = false;
							Token currentToken = tokenizer.currentToken();
							if (currentToken instanceof WordToken || currentToken instanceof StringToken || currentToken instanceof NumberToken) {
								isCast = true;
							} else if (checkSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
								isCast = true;
							}

							if (isCast) {
								tokenizer.commit();

								HiNode typeNode = new NodeType(type);
								operands.add(typeNode);
								operations.addPrefixOperation(OperationType.CAST);
								return true;
							}
						}
					}
				} else {
					// for cases (int[]), (A.B.C[][]), (A[]) and so on
					tokenizer.commit();
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					HiNode typeNode = new NodeType(type);
					operands.add(typeNode);
					operations.addPrefixOperation(OperationType.CAST);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}

	public boolean visitArrayIndex(Tokenizer tokenizer, OperationsGroup operations, List<HiNodeIF> operands, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		if (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			Token token = tokenizer.currentToken();

			HiNode indexNode = methodPriority.visit(tokenizer, ctx);
			if (indexNode == null) {
				tokenizer.error("expression expected", token);
			}

			expectSymbol(tokenizer, Symbols.SQUARE_BRACES_RIGHT);

			operands.add(indexNode);
			operations.addPostfixOperation(OperationType.ARRAY_INDEX);
			return true;
		}
		return false;
	}

	public boolean visitArrayIndexes(Tokenizer tokenizer, OperationsGroup operations, List<HiNodeIF> operands, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		boolean found = false;
		while (visitArrayIndex(tokenizer, operations, operands, ctx)) {
			found = true;
		}
		return found;
	}

	protected boolean visitSimpleExpression(Tokenizer tokenizer, List<OperationsGroup> allOperations, List<HiNodeIF> operands, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);

		// visit number
		NodeNumber numberNode = visitNumber(tokenizer);
		if (numberNode != null) {
			operands.add(numberNode);
			return true;
		}

		// visit boolean
		int boolType = visitWordType(tokenizer, TRUE, FALSE);
		if (boolType != -1) {
			operands.add(NodeBoolean.getInstance(boolType == TRUE, startToken));
			return true;
		}

		// visit null
		if (visitWordType(tokenizer, NULL) != -1) {
			operands.add(NodeNull.instance);
			return true;
		}

		// visit this
		if (visitWordType(tokenizer, THIS) != -1) {
			operands.add(new NodeThis());
			return true;
		}

		// visit super
		if (visitWordType(tokenizer, SUPER) != -1) {
			operands.add(new NodeSuper());
			return true;
		}

		// visit class
		if (visitWordType(tokenizer, CLASS) != -1) {
			operands.add(new NodeGetClass());
			return true;
		}

		// visit character
		HiNodeIF node;
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

		// visit lambda
		if ((node = LambdaParseRule.getInstance().visit(tokenizer, ctx)) != null) {
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
			node = methodPriority.visit(tokenizer, ctx);
			if (node != null) {
				expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);
				operands.add(node);
			} else {
				tokenizer.error("expression expected");
			}
			return true;
		}

		// visit casted identifier: A<O|? super O|? extends O>(B b, c, var d, var _, _) a
		boolean visitCastAfterIdentifier = isVisitCastAfterIdentifier(allOperations);
		if (visitCastAfterIdentifier && (node = visitIdentifier(tokenizer, ctx, true, true, false)) != null) {
			operands.add(node);
			return true;
		}

		// visit method name with arguments
		if ((node = InvocationParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			// determine is there prefix before method
			OperationsGroup lastOperationGroup = allOperations.size() > 0 ? allOperations.get(allOperations.size() - 1) : null;
			HiOperation lastOperation = lastOperationGroup != null ? lastOperationGroup.getOperation() : null;
			boolean inner = lastOperation == null || lastOperation.getOperation() != OperationType.INVOCATION;

			((NodeInvocation) node).setInner(inner);
			operands.add(node);
			return true;
		}

		// @unnamed
		if (visitWord(tokenizer, UNNAMED_VARIABLE) != null) {
			NodeIdentifier identifier = new NodeIdentifier(NodeIdentifier.UNNAMED, 0);
			identifier.setToken(startToken);
			operands.add(identifier);
			return true;
		}

		// visit identifier as word: package, class, method, field
		if ((node = visitIdentifier(tokenizer, ctx, visitCastAfterIdentifier, false, false)) != null) {
			operands.add(node);
			return true;
		}
		return false;
	}

	private boolean isVisitCastAfterIdentifier(List<OperationsGroup> allOperations) {
		if (allOperations.size() > 0) {
			int index = allOperations.size() - 1;
			OperationsGroup lastOperation = allOperations.get(index);
			while (lastOperation.getOperation() != null && lastOperation.getOperation().getOperation() == OperationType.INVOCATION) {
				lastOperation = allOperations.get(--index);
			}
			if ((lastOperation == null || !lastOperation.hasOperations()) && priority == Priority.castedIdentifier) {
				return true;
			} else if (lastOperation != null && lastOperation.getOperation() != null && lastOperation.getOperation().getOperation() == OperationType.INSTANCE_OF) {
				return true;
			}
		}
		return false;
	}

	private OperationType visitOperation(Tokenizer tokenizer, List<HiNodeIF> operands) throws TokenizerException {
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
				return OperationType.INSTANCE_OF;
			}
		} else if (currentToken instanceof NumberToken && operands.size() > 0 && ((NumberToken) currentToken).hasSign()) {
			return OperationType.PLUS;
		}
		return null;
	}
}
