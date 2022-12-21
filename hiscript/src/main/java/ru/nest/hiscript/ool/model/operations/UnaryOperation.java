package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public abstract class UnaryOperation extends HiOperation {
	UnaryOperation(String name, int operation) {
		super(name, 1, operation);
	}

	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeValueType node) {
		return null;
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeValueType... nodes) {
		NodeExpressionNoLS.NodeValueType node = nodes[0];
		if (prepareOperationResultType(validationInfo, ctx, node)) {
			node.type = getOperationResultType(validationInfo, ctx, node);
			node.isValue = node.isValue && node.isValue;
		}
	}

	protected boolean prepareOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeValueType node) {
		if (node.type == null) {
			node.type = node.getType(validationInfo, ctx);
			if (node.type != null) {
				node.isValue = node.node.isValue();
				return true;
			}
		}
		return false;
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v = values[0];
		if (v.valueType == Value.NAME) {
			NodeIdentifier.resolve(ctx, v, true);
		}
		doOperation(ctx, v);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v);

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type) {
		// operator '<operator>' can not be applied to <type>
	}
}
