package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.Operation;
import script.ool.model.Operations;
import script.ool.model.OperationsGroup;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeExpression extends Node {
	/**
	 * <operations1> X1 <operations2> X2 ... <operations n> Xn <operations n+1>
	 */
	public NodeExpression(Node[] operands, OperationsGroup[] operations, int codeLine) {
		super("expression", TYPE_EXPRESSION, codeLine);
		assert operands.length == operations.length - 1;
		compile(operands, operations);
	}

	private NodeExpression(Node[] operands, Operation[] operations) {
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

		Operation[] buf = new Operation[operationsCount];
		int bufSize = 0;
		int pos = 1;
		for (int i = 1; i < o.length; i++) {
			OperationsGroup og_prev = o[i - 1];
			OperationsGroup og = o[i];

			// compare latest operation with current group operation having min priority
			if (bufSize > 0) {
				Operation lo = buf[bufSize - 1];
				if (lo != null) {
					int curentGroupMinPriority = og.getMinPriority();
					while (lo.getPriority() <= curentGroupMinPriority) {
						operations[pos++] = lo; // operation
						bufSize--;
						if (bufSize == 0) {
							break;
						}
						lo = buf[bufSize - 1];
					}
				}
			}

			bufSize = og_prev.appendPrefix(buf, bufSize);
			pos += og_prev.getPrefixOperandsCount();

			// check after appending prefixes
			if (bufSize > 0) {
				Operation lo = buf[bufSize - 1];
				if (lo != null) {
					int curentGroupMinPriority = og.getMinPriority();
					while (lo.getPriority() <= curentGroupMinPriority) {
						operations[pos++] = lo; // operation
						bufSize--;
						if (bufSize == 0) {
							break;
						}
						lo = buf[bufSize - 1];
					}
				}
			}

			// append postfixes
			if (og.postfix != null) {
				int l = og.postfix.size();
				for (int j = 0; j < l; j++) {
					Operation postOper = og.postfix.get(j);
					pos += postOper.getOperandsCount() - 1;
					operations[pos++] = postOper; // operation
				}
			}

			// append operation
			if (og.getOperation() != null && og.getOperation().getOperation() == Operations.SKIP) {
				operations[pos++] = null; // operation
			} else {
				bufSize = og.append(buf, bufSize);
				pos += og.getOperandsCount();
			}
		}

		while (bufSize != 0) {
			operations[pos++] = buf[--bufSize];
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

	public void execute(RuntimeContext ctx) {
		Value[] values = ctx.getValues(operands.length);
		try {
			int bufSize = 0;
			int valuePos = 0;
			for (int i = 0; i < operations.length; i++) {
				if (operations[i] == null) {
					// get value
					Value oldValue = ctx.value;
					try {
						ctx.value = values[bufSize];
						Node valueNode = operands[valuePos];

						// Check for a.new B()
						boolean executeLater = false;
						if (bufSize > 0 && i < operations.length - 1 && operations[i + 1] != null && operations[i + 1].getOperation() == Operation.INVOCATION) {
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
						ctx.value = oldValue;
					}
				} else {
					int skipToOperation = -1;
					if (operations[i].getOperation() == Operations.LOGICAL_AND_CHECK) {
						if (!values[bufSize - 1].bool) {
							skipToOperation = Operations.LOGICAL_AND;
						}
					} else if (operations[i].getOperation() == Operations.LOGICAL_OR_CHECK) {
						if (values[bufSize - 1].bool) {
							skipToOperation = Operations.LOGICAL_OR;
						}
					}

					if (skipToOperation != -1) {
						i++;
						while (operations[i] == null || operations[i].getOperation() != skipToOperation) {
							if (operations[i] == null) {
								bufSize++;
								valuePos++;
							} else {
								if (operations[i].getOperation() != Operations.LOGICAL_AND_CHECK && operations[i].getOperation() != Operations.LOGICAL_OR_CHECK) {
									bufSize = operations[i].skipOperation(ctx, bufSize);
								}
							}
							i++;
						}
						while (i < operations.length && operations[i].getOperation() == skipToOperation) {
							bufSize = operations[i].skipOperation(ctx, bufSize);
							i++;
						}
					} else {
						// do operation
						bufSize = operations[i].doOperation(ctx, bufSize, values);
					}
				}

				if (ctx.exitFromBlock()) {
					return;
				}
			}

			if (values[0].valueType == Value.NAME) {
				if (!NodeIdentificator.resolveVariable(ctx, values[0], true)) {
					ctx.throwException("can't resolve variable " + values[0].name);
					return;
				}
			}

			values[0].copyTo(ctx.value);
		} finally {
			ctx.putValues(values);
		}
	}

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

	public static NodeExpression decode(DecodeContext os) throws IOException {
		Node[] operands = os.readArray(Node.class, os.readShort());

		Operation[] operations = new Operation[os.readShort()];
		for (int i = 0; i < operations.length; i++) {
			int operationType = os.readByte();
			if (operationType != -1) {
				operations[i] = Operations.getOperation(operationType);
			}
		}

		return new NodeExpression(operands, operations);
	}
}
