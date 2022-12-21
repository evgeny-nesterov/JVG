package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public abstract class HiOperation implements PrimitiveTypes, OperationsIF {
	protected HiOperation(String name, int operandsCount, int operation) {
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

	@Override
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

	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType... nodes) {
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
		}
		return index;
	}

	public int getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, int index, NodeExpressionNoLS.NodeOperandType... nodes) {
		switch (operandsCount) {
			case 1:
				getOperationResultType(validationInfo, ctx, nodes[index - 1]);
				return index;
			case 2:
				getOperationResultType(validationInfo, ctx, nodes[index - 2], nodes[index - 1]);
				return index - 1;
		}
		return index;
	}

	public int getOperationBufIndex(int index) {
		switch (operandsCount) {
			case 1:
				return index;
			case 2:
				return index - 1;
		}
		return index;
	}

	public void errorIncompatibleTypes(RuntimeContext ctx, HiClass type1, HiClass type2) {
		String text = "incompatible types; found " + type1.fullName + ", required " + type2.fullName;
		ctx.throwRuntimeException(text);
	}

	public void code(CodeContext os) throws IOException {
		// TODO: write start operation data block
		os.writeByte(operation);
	}
}