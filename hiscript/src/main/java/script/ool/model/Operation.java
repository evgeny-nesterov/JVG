package script.ool.model;

import java.io.IOException;

import script.ool.model.nodes.CodeContext;

public abstract class Operation implements PrimitiveTypes, OperationsIF {
	protected Operation(String name, int operandsCount, int operation) {
		this.name = name;
		this.operandsCount = operandsCount;
		this.operation = operation;
		this.priority = Operations.getPriority(operation);
		this.increment = operandsCount == 1 ? 0 : 1;
	}

	protected int operation;

	public int getOperation() {
		return operation;
	}

	protected int priority;

	public int getPriority() {
		return priority;
	}

	protected String name;

	public String getName() {
		return name;
	}

	public String toString() {
		return name;
	}

	private int operandsCount;

	public int getOperandsCount() {
		return operandsCount;
	}

	private int increment;

	public int getIncrement() {
		return increment;
	}

	public abstract void doOperation(RuntimeContext ctx, Value... values);

	public int doOperation(RuntimeContext ctx, int index, Value... values) {
		switch (operandsCount) {
			case 1:
				doOperation(ctx, values[index - 1]);
				return index;

			case 2:
				doOperation(ctx, values[index - 2], values[index - 1]);
				return index - 1;

			case 3:
				doOperation(ctx, values[index - 3], values[index - 2], values[index - 1]);
				return index - 3;
		}
		return index;
	}

	public int skipOperation(RuntimeContext ctx, int index) {
		switch (operandsCount) {
			case 1:
				return index;

			case 2:
				return index - 1;

			case 3:
				return index - 3;
		}
		return index;
	}

	public void errorIncompatibleTypes(RuntimeContext ctx, Clazz type1, Clazz type2) {
		String text = "incompatible types; found " + type1.fullName + ", required " + type2.fullName;
		ctx.throwException(text);
	}

	public void code(CodeContext os) throws IOException {
		// TODO: write start operation data block
		os.writeByte(operation);
	}
}
