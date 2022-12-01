package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.Operations;
import ru.nest.hiscript.ool.model.OperationsGroup;
import ru.nest.hiscript.ool.model.OperationsIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeExpressionNoLS extends NodeExpression {
	/**
	 * <operations1> X1 <operations2> X2 ... <operations n> Xn <operations n+1>
	 */
	public NodeExpressionNoLS(Node[] operands, OperationsGroup[] operations, int codeLine) {
		super("expression", TYPE_EXPRESSION, codeLine);
		compile(operands, operations);
	}

	private NodeExpressionNoLS(Node[] operands, Operation[] operations) {
		super("expression", TYPE_EXPRESSION);
		this.operands = operands;
		this.operations = operations;
	}

	private void compile(Node[] operands, OperationsGroup[] o) {
		this.operands = operands;
		int operandsCount = operands.length;

		int operationsCount = 0;
		for (int i = 0; i < o.length; i++) {
			operationsCount += o[i].getCount();
		}
		operations = new Operation[operandsCount + operationsCount];

		Operation[] stack = new Operation[operationsCount];
		int stackSize = 0;
		int pos = 1;
		for (int i = 0; i < o.length; i++) {
			OperationsGroup og = o[i];

			// check after appending prefixes
			if (stackSize > 0) {
				Operation lastStackOperation = stack[stackSize - 1];
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
					Operation postOperation = og.postfix.get(j);

					// prev operation may has same priority (for example, object.getArray()[index])
					if (stackSize > 0) {
						Operation lastStackOperation = stack[stackSize - 1];
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

	private Node[] operands;

	private Operation[] operations;

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
						Node valueNode = operands[valuePos];

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
									bufSize = operations[i].skipOperation(ctx, bufSize);
								}
							}
							i++;
						}
						while (i < operations.length && operations[i].getOperation() == skipToOperation) {
							bufSize = operations[i].skipOperation(ctx, bufSize);
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
				if (!NodeIdentifier.resolveVariable(ctx, values[0], true)) {
					ctx.throwRuntimeException("can't resolve variable " + values[0].name);
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
		os.writeShort(operands.length);
		os.write(operands);

		int operationsCount = operations.length;
		os.writeShort(operationsCount);
		for (int i = 0; i < operationsCount; i++) {
			os.writeByte(operations[i] != null ? operations[i].getOperation() : -1);
		}
	}

	public static NodeExpressionNoLS decode(DecodeContext os) throws IOException {
		Node[] operands = os.readArray(Node.class, os.readShort());

		Operation[] operations = new Operation[os.readShort()];
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
}
