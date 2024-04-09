package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationsIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeExpressionNoLS extends NodeExpression {
	/**
	 * <operations1> X1 <operations2> X2 ... <operations n> Xn <operations n+1>
	 */
	public NodeExpressionNoLS(HiNodeIF[] operands, OperationsGroup[] operations) {
		super("expression", TYPE_EXPRESSION);
		compile(operands, operations);
	}

	private NodeExpressionNoLS(HiNodeIF[] operands, HiOperation[] operations) {
		super("expression", TYPE_EXPRESSION);
		this.operands = operands;
		this.operations = operations;
	}

	private void compile(HiNodeIF[] operands, OperationsGroup[] o) {
		this.operands = operands;
		int operandsCount = operands.length;

		int operationsCount = 0;
		for (int i = 0; i < o.length; i++) {
			operationsCount += o[i].getCount();
		}
		operations = new HiOperation[operandsCount + operationsCount];

		HiOperation[] stack = new HiOperation[operationsCount];
		int stackSize = 0;
		int pos = 1;
		for (int i = 0; i < o.length; i++) {
			OperationsGroup og = o[i];

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

		// DEBUG
		// print();
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

	private HiNodeIF[] operands;

	private HiOperation[] operations;

	@Override
	protected void computeValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			ctx.nodeValueType.get(validationInfo, ctx, operands[0]);
			return;
		}

		NodeValueType[] nodes = ctx.getNodesValueTypesCache(operands.length);
		if (operands.length == 0) {
			validationInfo.error("invalid expression", getToken());
			return;
		}

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
				bufSize = operations[i].getOperationResultType(validationInfo, ctx, bufSize, nodes);
				nodes[bufSize - 1].node = null;
			}
		}
		nodes[0].copyTo(ctx.nodeValueType);
		ctx.putNodesValueTypesCache(nodes);
		ctx.nodeValueType.resolvedValueVariable = this;
		ctx.nodeValueType.enclosingClass = ctx.nodeValueType.type;

		if (!validValue && validationInfo.messages.size() == 0) {
			validationInfo.error("invalid expression", getToken());
		}
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		NodeValueType resultValueType = getValueType(validationInfo, ctx);
		boolean valid = resultValueType.valid;
		if (resultValueType.isCompileValue()) {
			// TODO simplify expression
		}

		valid &= super.validate(validationInfo, ctx);
		return valid;
	}

	public boolean resolveValue(RuntimeContext ctx, Value value) {
		if (value.valueType == Value.NAME) {
			if (!NodeIdentifier.resolve(ctx, value, true)) {
				if (value.nameDimensions == 0) {
					ctx.throwRuntimeException("cannot resolve variable " + value.name);
				} else {
					ctx.throwRuntimeException("cannot resolve class " + value.name);
				}
				return false;
			}
		}

		// autobox
		if (value.type.getAutoboxedPrimitiveClass() != null) {
			value.substitutePrimitiveValueFromAutoboxValue();
		}

		value.copyTo(ctx.value);
		return true;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// optimization
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			operands[0].execute(ctx);
			resolveValue(ctx, ctx.value);
			return;
		}

		Value ctxValue = ctx.value;
		Value[] values = ctx.getValues(operands.length);
		try {
			int bufSize = 0;
			int valuePos = 0;
			for (int i = 0; i < operations.length; i++) {
				if (operations[i] == null) {
					// get value
					try {
						ctx.value = values[bufSize];
						HiNodeIF valueNode = operands[valuePos];

						// Check for a.new B()
						boolean executeLater = false;
						if (bufSize > 0 && i < operations.length - 1 && operations[i + 1] != null && operations[i + 1].getOperation() == OperationsIF.INVOCATION) {
							if (valueNode.getInvocationValueType() != -1) {
								executeLater = true;
								// Previous operand may be not calculated yet
								// For example, in the case of Value.NAME
								ctx.value.valueType = valueNode.getInvocationValueType();
								ctx.value.node = valueNode;
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
					int skipToOperation = -1;
					// TODO do not check && and || of inner blocks
					if (operations[i].getOperation() == OperationsIF.LOGICAL_AND_CHECK) {
						Value lastValue = values[bufSize - 1];
						resolveValue(ctx, lastValue);
						if (!lastValue.bool) {
							skipToOperation = OperationsIF.LOGICAL_AND;
						}
					} else if (operations[i].getOperation() == OperationsIF.LOGICAL_OR_CHECK) {
						Value lastValue = values[bufSize - 1];
						resolveValue(ctx, lastValue);
						if (lastValue.bool) {
							skipToOperation = OperationsIF.LOGICAL_OR;
						}
					}

					if (skipToOperation != -1) {
						i++;
						while (operations[i] == null || operations[i].getOperation() != skipToOperation) {
							if (operations[i] == null) {
								bufSize++;
								valuePos++;
							} else {
								if (operations[i].getOperation() != OperationsIF.LOGICAL_AND_CHECK && operations[i].getOperation() != OperationsIF.LOGICAL_OR_CHECK) {
									bufSize = operations[i].getOperationBufIndex(bufSize);
								}
							}
							i++;
						}
						while (i < operations.length && operations[i].getOperation() == skipToOperation) {
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

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShortArray(operands);

		int operationsCount = operations.length;
		os.writeShort(operationsCount);
		for (int i = 0; i < operationsCount; i++) {
			os.writeByte(operations[i] != null ? operations[i].getOperation() : -1);
		}
	}

	public static NodeExpressionNoLS decode(DecodeContext os) throws IOException {
		HiNode[] operands = os.readShortArray(HiNode.class);

		HiOperation[] operations = new HiOperation[os.readShort()];
		for (int i = 0; i < operations.length; i++) {
			int operationType = os.readByte();
			if (operationType != -1) {
				operations[i] = Operations.getOperation(operationType);
			}
		}
		return new NodeExpressionNoLS(operands, operations);
	}

	public NodeIdentifier checkIdentifier() {
		if (operations.length == 1 && operations[0] == null && operands.length == 1 && operands[0] instanceof NodeIdentifier) {
			return (NodeIdentifier) operands[0];
		}
		return null;
	}

	public NodeCastedIdentifier checkCastedIdentifier() {
		if (operations.length == 1 && operations[0] == null && operands.length == 1 && operands[0] instanceof NodeCastedIdentifier) {
			return (NodeCastedIdentifier) operands[0];
		}
		return null;
	}

	public HiMethod checkMethod() {
		if (operations.length == 1 && operations[0] == null && operands.length == 1 && operands[0] instanceof HiMethod) {
			return (HiMethod) operands[0];
		}
		return null;
	}
}
