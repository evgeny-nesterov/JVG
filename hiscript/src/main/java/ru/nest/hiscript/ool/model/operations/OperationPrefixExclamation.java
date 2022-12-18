package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPrefixExclamation extends UnaryOperation {
	private static Operation instance = new OperationPrefixExclamation();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixExclamation() {
		super("!", PREFIX_EXCLAMATION);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node) {
		if (!node.type.isPrimitive() && HiFieldPrimitive.getType(node.type) != BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.type.fullName + "'", node.node.getToken());
		}
		return node.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.type;
		if (c != TYPE_BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}
		v.bool = !v.bool;
	}
}
