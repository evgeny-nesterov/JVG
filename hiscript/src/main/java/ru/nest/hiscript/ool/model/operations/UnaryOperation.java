package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public abstract class UnaryOperation extends HiOperation {
	UnaryOperation(int operation) {
		super(1, operation);
	}

	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		return null;
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType... nodes) {
		NodeValueType node = nodes[0];
		if (prepareOperationResultType(validationInfo, ctx, node)) {
			node.clazz = getOperationResultType(validationInfo, ctx, node);
			if (!node.valid) {
				node.returnType = null;
			}
		}
	}

	protected boolean prepareOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		if (node.clazz == null) {
			node.clazz = node.get(validationInfo, ctx).clazz;
			if (node.clazz != null) {
				node.returnType = node.node.getValueReturnType();
				return true;
			}
		}
		return false;
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v = values[0];
		if (v.valueType == Value.NAME) {
			NodeIdentifier.resolve(ctx, v);
		}
		doOperation(ctx, v);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v);

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type) {
		// operator '<operator>' can not be applied to <type>
	}
}
