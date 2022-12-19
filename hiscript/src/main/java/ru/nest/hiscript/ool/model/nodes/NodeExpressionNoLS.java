package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
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
	public NodeExpressionNoLS(HiNode[] operands, OperationsGroup[] operations) {
		super("expression", TYPE_EXPRESSION);
		compile(operands, operations);
	}

	private NodeExpressionNoLS(HiNode[] operands, HiOperation[] operations) {
		super("expression", TYPE_EXPRESSION);
		this.operands = operands;
		this.operations = operations;
	}

	private void compile(HiNode[] operands, OperationsGroup[] o) {
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

			// check after appending prefixes
			if (stackSize > 0) {
				HiOperation lastStackOperation = stack[stackSize - 1];
				if (lastStackOperation != null) {
					int currentGroupMinPriority = og.getMinPriority();
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

			// append postfixes
			// priority of postfixes has to be greater or equals (for example, . and []) of operation priority
			if (og.postfix != null) {
				int l = og.postfix.size();
				for (int j = 0; j < l; j++) {
					HiOperation postOperation = og.postfix.get(j);

					// prev operation may has same priority (for example, object.getArray()[index])
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
					operations[pos++] = postOperation; // operation
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

	private HiNode[] operands;

	private HiOperation[] operations;

	public static class NodeOperandType {
		public HiNode node;

		public HiClass type;

		public boolean isValue;

		NodeOperandType(HiNode node) {
			this.node = node;
		}

		public HiClass getType(ValidationInfo validationInfo, CompileClassContext ctx) {
			return node.getValueType(validationInfo, ctx);
		}
	}

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (operands.length == 1 && operations.length == 1 && operations[0] == null) {
			return operands[0].getValueType(validationInfo, ctx);
		}

		NodeOperandType[] nodes = new NodeOperandType[operands.length];
		int bufSize = 0;
		int valuePos = 0;
		for (int i = 0; i < operations.length; i++) {
			if (operations[i] == null) {
				// get value
				HiNode valueNode = operands[valuePos];
				nodes[bufSize] = new NodeOperandType(valueNode);
				bufSize++;
				valuePos++;
			} else {
				// do operation
				bufSize = operations[i].getOperationResultType(validationInfo, ctx, bufSize, nodes);
				if (nodes[bufSize - 1].type == null) {
					return null;
				}
			}
		}
		if (nodes[0].isValue) {
			// TODO simplify expression
		}
		return nodes[0].type;
	}

	private HiClass resultClass;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		if (valid) {
			resultClass = getValueType(validationInfo, ctx);
			valid = resultClass != null;
		}
		for (int i = 0; i < operands.length; i++) {
			valid &= operands[i].validate(validationInfo, ctx);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
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
						HiNode valueNode = operands[valuePos];

						// Check for a.new B()
						boolean executeLater = false;
						if (bufSize > 0 && i < operations.length - 1 && operations[i + 1] != null && operations[i + 1].getOperation() == OperationsIF.INVOCATION) {
							if (valueNode instanceof NodeConstructor || valueNode instanceof NodeArray || valueNode instanceof NodeArrayValue) {
								executeLater = true;
								// Previous operand may be not calculated yet
								// For example, in the case of Value.NAME
								ctx.value.valueType = Value.EXECUTE;
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
						if (!values[bufSize - 1].bool) {
							skipToOperation = OperationsIF.LOGICAL_AND;
						}
					} else if (operations[i].getOperation() == OperationsIF.LOGICAL_OR_CHECK) {
						if (values[bufSize - 1].bool) {
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

			if (values[0].valueType == Value.NAME) {
				if (!NodeIdentifier.resolve(ctx, values[0], true)) {
					if (values[0].nameDimensions == 0) {
						ctx.throwRuntimeException("Can't resolve variable " + values[0].name);
					} else {
						ctx.throwRuntimeException("Can't resolve class " + values[0].name);
					}
					return;
				}
			}

			values[0].copyTo(ctx.value);
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
}
