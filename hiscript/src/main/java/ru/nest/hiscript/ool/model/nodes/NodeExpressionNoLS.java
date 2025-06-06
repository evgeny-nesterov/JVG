package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import java.io.IOException;
import java.util.List;

public class NodeExpressionNoLS extends NodeExpression {
	/**
	 * <operations1> X1 <operations2> X2 ... <operations n> Xn <operations n+1>
	 */
	public NodeExpressionNoLS(HiNodeIF[] operands, HiOperation[] operations) {
		super("expression", TYPE_EXPRESSION);
		this.operands = operands;
		this.operations = operations;
	}

	private HiNodeIF[] operands;

	private HiOperation[] operations;

	public static HiOperation[] compile(HiNodeIF[] operands, List<OperationsGroup> allOperations) {
		int operandsCount = operands.length;

		int operationsCount = 0;
		for (int i = 0; i < allOperations.size(); i++) {
			operationsCount += allOperations.get(i).getCount();
		}
		HiOperation[] operations = new HiOperation[operandsCount + operationsCount];

		HiOperation[] stack = new HiOperation[operationsCount];
		int stackSize = 0;
		int pos = 1;
		for (int i = 0; i < allOperations.size(); i++) {
			OperationsGroup og = allOperations.get(i);

			// append postfixes
			// priority of postfixes has to be greater or equals (for example, . and []) of operation priority
			if (og.postfix != null) {
				int l = og.postfix.size();
				for (int j = 0; j < l; j++) {
					HiOperation postOperation = og.postfix.get(j);

					// prev operation may have same priority (for example, object.getArray()[index])
					if (stackSize > 0) {
						HiOperation lastStackOperation = stack[stackSize - 1];
						if (lastStackOperation != null) {
							while (lastStackOperation.getPriority() <= postOperation.getPriority()) { // < is the error!
								operations[pos++] = lastStackOperation; // operation
								stackSize--;
								if (stackSize == 0) {
									break;
								}
								lastStackOperation = stack[stackSize - 1];
							}
						}
					}

					pos += postOperation.getOperandsCount() - 1;
					if (pos < operations.length) {
						operations[pos++] = postOperation; // operation
					}
				}
			}

			// check after appending prefixes
			if (stackSize > 0) {
				HiOperation lastStackOperation = stack[stackSize - 1];
				if (lastStackOperation != null && og.getOperation() != null) {
					int currentGroupMinPriority = og.getOperation().getPriority();
					while (lastStackOperation.getPriority() <= currentGroupMinPriority) {
						operations[pos++] = lastStackOperation; // operation
						stackSize--;
						if (stackSize == 0) {
							break;
						}
						lastStackOperation = stack[stackSize - 1];
					}
				}
			}

			stackSize = og.append(stack, stackSize);
			pos += og.getOperandsCount();

			// priority of prefixes has to be greater of operation priority
			stackSize = og.appendPrefix(stack, stackSize);
			pos += og.getPrefixOperandsCount();
		}

		while (stackSize != 0 && pos < operations.length) {
			operations[pos++] = stack[--stackSize];
		}
		return operations;
	}

	@SuppressWarnings("unused")
	private void print() {
		int pos = 0;
		for (int i = 0; i < operations.length; i++) {
			if (operations[i] == null) {
				if (pos < operands.length) {
					System.out.print(operands[pos++] + " ");
				} else {
					System.out.print("??? ");
				}
			} else {
				System.out.print(operations[i] + " ");
			}
		}
		System.out.println();
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		HiNode singleNode = getSingleNode();
		return singleNode != null && singleNode.isConstant(ctx);
	}

	@Override
	public Object getConstantValue() {
		HiNode singleNode = getSingleNode();
		return singleNode != null ? singleNode.getConstantValue() : null;
	}

	@Override
	public int getArrayDimension() {
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			HiNodeIF operand = operands[0];
			return operand.getArrayDimension();
		}
		return 0;
	}

	@Override
	protected void computeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			HiNodeIF operand = operands[0];
			if (isStatement() && !operand.isStatement()) {
				validationInfo.error("not a statement", operand);
			} else {
				ctx.nodeValueType.get(validationInfo, ctx, operand);
			}
			return;
		}

		if (isStatement() && operations[operations.length - 1] != null && !operations[operations.length - 1].isStatement()) {
			validationInfo.error("not a statement", getToken());
			return;
		}
		if (operands.length == 0) {
			validationInfo.error("invalid expression", getToken());
			return;
		}

		NodeValueType[] nodes = ctx.getNodesValueTypesCache(operands.length);
		int bufSize = 0;
		int valuePos = 0;
		boolean validValue = true;
		for (int i = 0; i < operations.length; i++) {
			if (operations[i] == null) {
				// get value
				if (valuePos < operands.length) {
					HiNodeIF valueNode = operands[valuePos];
					if (valueNode != null) {
						nodes[bufSize].init(valueNode);
					}
				} else {
					validValue = false;
				}
				bufSize++;
				valuePos++;
			} else {
				// do operation and write result to first node
				HiOperation operation = operations[i];
				int messagesCount = validationInfo.messages.size();
				bufSize = operation.getOperationResultType(validationInfo, ctx, bufSize, nodes);
				if (nodes[bufSize - 1].isCompileValue() && validationInfo.messages.size() == messagesCount) {
					// collapse compiled value
					NodeValueType nodeType = nodes[bufSize - 1];
					HiNodeIF node = null;
					if (nodeType.valueClass == HiClassPrimitive.BYTE) {
						node = new NodeByte(nodeType.byteValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.SHORT) {
						node = new NodeShort(nodeType.shortValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.INT) {
						node = new NodeInt(nodeType.intValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.LONG) {
						node = new NodeLong(nodeType.longValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.FLOAT) {
						node = new NodeFloat(nodeType.floatValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.DOUBLE) {
						node = new NodeDouble(nodeType.doubleValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.CHAR) {
						node = new NodeChar(nodeType.charValue, null);
					} else if (nodeType.valueClass == HiClassPrimitive.BOOLEAN) {
						node = NodeBoolean.getInstance(nodeType.booleanValue, null);
					} else if (nodeType.valueClass == HiClass.STRING_CLASS) {
						node = new NodeString(nodeType.stringValue, null);
					}

					if (operation.getOperandsCount() == 2) {
						operands[valuePos - 2] = node;

						HiNodeIF[] collapsedOperands = new HiNodeIF[operands.length - 1];
						int operandPos = 0;
						for (int j = 0; j < operands.length; j++) {
							if (j != valuePos - 1) {
								collapsedOperands[operandPos++] = operands[j];
							}
						}
						operands = collapsedOperands;

						HiOperation[] collapsedOperations = new HiOperation[operations.length - 2];
						int operationPos = 0;
						for (int j = 0; j < operations.length; j++) {
							if (j != i && j != i - 1) {
								collapsedOperations[operationPos++] = operations[j];
							}
						}
						operations = collapsedOperations;

						i -= 3;
						valuePos -= 2;
						bufSize--;
					} else { // operation.getOperandsCount() == 1
						operands[valuePos - 1] = node;

						HiOperation[] collapsedOperations = new HiOperation[operations.length - 1];
						int operationPos = 0;
						for (int j = 0; j < operations.length; j++) {
							if (j != i) {
								collapsedOperations[operationPos++] = operations[j];
							}
						}
						operations = collapsedOperations;

						i -= 2;
						valuePos -= 1;
						bufSize--;
					}
					if (operations.length == 1) {
						break;
					}
					continue;
				}
				nodes[bufSize - 1].node = null;
			}
		}
		nodes[0].copyTo(ctx.nodeValueType);
		ctx.putNodesValueTypesCache(nodes);
		ctx.nodeValueType.node = this;
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = ctx.nodeValueType.clazz;
		ctx.nodeValueType.enclosingType = ctx.nodeValueType.type;

		// TODO remove?
		if (!validValue && validationInfo.messages.size() == 0) {
			validationInfo.error("invalid expression", getToken());
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		NodeValueType resultValueType = getNodeValueType(validationInfo, ctx);
		boolean valid = resultValueType.valid;
		if (resultValueType.isCompileValue()) {
			// TODO simplify expression
		}

		valid &= super.validate(validationInfo, ctx);
		return valid;
	}

	public void resolveValue(RuntimeContext ctx, Value value) {
		if (value.valueType == ValueType.NAME) {
			assert NodeIdentifier.resolve(ctx, value); // node resolved in validation
			if (ctx.exitFromBlock()) {
				return;
			}
		}

		// @autoboxing
		if (value.valueClass.getAutoboxedPrimitiveClass() != null && value.object != null) {
			value.substitutePrimitiveValueFromAutoboxValue();
		}

		value.copyTo(ctx.value);
	}

	public <N extends HiNodeIF> N getSingleNode() {
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			return (N) operands[0];
		}
		return null;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// optimization
		HiNodeIF singleNode = getSingleNode();
		if (singleNode != null) {
			singleNode.execute(ctx);
			resolveValue(ctx, ctx.value);
			return;
		}

		Value ctxValue = ctx.value;
		Value[] values = ctx.getValues(operands.length);
		try {
			int bufSize = 0;
			int valuePos = 0;
			int operationsCount = operations.length;
			for (int i = 0; i < operationsCount; i++) {
				if (operations[i] == null) {
					// get value
					try {
						ctx.value = values[bufSize];
						HiNodeIF valueNode = operands[valuePos];
						ctx.value.node = valueNode;

						// Check for a.new B()
						boolean executeLater = false;
						if (bufSize > 0 && i < operationsCount - 1 && operations[i + 1] != null && operations[i + 1].getOperation() == OperationType.INVOCATION) {
							if (valueNode.getInvocationValueType() != ValueType.UNDEFINED) {
								executeLater = true;
								// Previous operand may be not calculated yet
								// For example, in the case of ValueType.NAME
								ctx.value.valueType = valueNode.getInvocationValueType();
							}
						}

						if (!executeLater) {
							valueNode.execute(ctx);
						}

						bufSize++;
						valuePos++;
					} finally {
						ctx.value = ctxValue;
					}
				} else {
					OperationType skipToOperation = null;
					// TODO do not check && and || of inner blocks
					if (operations[i].getOperation() == OperationType.LOGICAL_AND_CHECK) {
						Value lastValue = values[bufSize - 1];
						resolveValue(ctx, lastValue);
						if (!lastValue.bool) {
							skipToOperation = OperationType.LOGICAL_AND;
						}
					} else if (operations[i].getOperation() == OperationType.LOGICAL_OR_CHECK) {
						Value lastValue = values[bufSize - 1];
						resolveValue(ctx, lastValue);
						if (lastValue.bool) {
							skipToOperation = OperationType.LOGICAL_OR;
						}
					}

					if (skipToOperation != null) {
						i++;
						while (operations[i] == null || operations[i].getOperation() != skipToOperation) {
							if (operations[i] == null) {
								bufSize++;
								valuePos++;
							} else {
								if (operations[i].getOperation() != OperationType.LOGICAL_AND_CHECK && operations[i].getOperation() != OperationType.LOGICAL_OR_CHECK) {
									bufSize = operations[i].getOperationBufIndex(bufSize);
								}
							}
							i++;
						}
						while (i < operationsCount && operations[i].getOperation() == skipToOperation) {
							bufSize = operations[i].getOperationBufIndex(bufSize);
							i++;
						}
						continue;
					}

					// do operation
					bufSize = operations[i].doOperation(ctx, bufSize, values);
				}

				if (ctx.exitFromBlock()) {
					return;
				}
			}

			resolveValue(ctx, values[0]);
		} finally {
			ctx.putValues(values);
		}
	}

	public NodeIdentifier checkIdentifier() {
		if (operations.length == 1 && operations[0] == null && operands.length == 1 && operands[0] instanceof NodeIdentifier) {
			return (NodeIdentifier) operands[0];
		}
		return null;
	}

	@Override
	public NodeCastedIdentifier checkCastedIdentifier() {
		if (isCastedIdentifier()) {
			return (NodeCastedIdentifier) operands[0];
		}
		return null;
	}

	@Override
	public boolean isCastedIdentifier() {
		return operations.length == 1 && operations[0] == null && operands.length == 1 && operands[0] instanceof NodeCastedIdentifier;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShortArray(operands);

		int operationsCount = operations.length;
		os.writeShort(operationsCount);
		for (int i = 0; i < operationsCount; i++) {
			os.writeEnum(operations[i] != null ? operations[i].getOperation() : null);
		}
	}

	public static NodeExpressionNoLS decode(DecodeContext os) throws IOException {
		HiNodeIF[] operands = os.readShortArray(HiNodeIF.class);

		HiOperation[] operations = new HiOperation[os.readShort()];
		for (int i = 0; i < operations.length; i++) {
			OperationType operationType = os.readEnum(OperationType.class);
			if (operationType != null) {
				operations[i] = Operations.getOperation(operationType);
			}
		}
		return new NodeExpressionNoLS(operands, operations);
	}
}
