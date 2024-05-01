package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeDeclaration;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public abstract class HiOperation implements PrimitiveTypes, OperationsIF {
	protected HiOperation(int operandsCount, int operation) {
		this.name = Operations.getName(operation);
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

	private final int operandsCount;

	public int getOperandsCount() {
		return operandsCount;
	}

	private final int increment;

	public int getIncrement() {
		return increment;
	}

	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType... nodes) {
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

	public int getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, int index, NodeValueType... nodes) {
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

	public void checkFinal(ValidationInfo validationInfo, CompileClassContext ctx, HiNodeIF node, boolean initialize) {
		HiNodeIF fieldNode = null;
		Modifiers modifiers = null;
		if (node instanceof NodeIdentifier) {
			NodeIdentifier identifierNode = (NodeIdentifier) node;
			Object resolvedIdentifier = ctx.resolveIdentifier(identifierNode.getName());
			if (resolvedIdentifier instanceof HiField) {
				HiField field = (HiField) resolvedIdentifier;
				modifiers = field.getModifiers();
				fieldNode = field;
			} else if (resolvedIdentifier instanceof NodeDeclaration) {
				NodeDeclaration declaration = ((NodeDeclaration) resolvedIdentifier);
				modifiers = declaration.getModifiers();
				fieldNode = declaration;
			} else if (resolvedIdentifier instanceof NodeArgument) {
				NodeArgument argument = ((NodeArgument) resolvedIdentifier);
				modifiers = argument.getModifiers();
				fieldNode = argument;
			} else if (resolvedIdentifier instanceof HiNode) {
				if (initialize) {
					ctx.initializedNodes.add((HiNode) resolvedIdentifier);
				}
			}
		} else if (node instanceof HiField) {
			HiField field = (HiField) node;
			modifiers = field.getModifiers();
			fieldNode = node;
		}
		if (modifiers != null) {
			if (modifiers.isFinal() && ctx.initializedNodes.contains(fieldNode)) {
				validationInfo.error("cannot assign value to final variable", node.getToken());
			} else if (initialize) {
				ctx.initializedNodes.add(fieldNode);
			}
		}
	}

	public boolean isStatement() {
		return false;
	}

	public void errorIncompatibleTypes(RuntimeContext ctx, HiClass type1, HiClass type2) {
		String text = "incompatible types; found " + type1.getNameDescr() + ", required " + type2.getNameDescr();
		ctx.throwRuntimeException(text);
	}

	public void code(CodeContext os) throws IOException {
		// TODO: write start operation data block
		os.writeByte(operation);
	}
}
