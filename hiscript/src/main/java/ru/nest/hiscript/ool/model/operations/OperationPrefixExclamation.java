package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPrefixExclamation extends UnaryOperation {
	private static final HiOperation instance = new OperationPrefixExclamation();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPrefixExclamation() {
		super("!", PREFIX_EXCLAMATION);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node) {
		HiClass type = node.type.getAutoboxedPrimitiveClass() == null ? node.type : node.type.getAutoboxedPrimitiveClass();
		if (!type.isPrimitive() && type.getPrimitiveType() != BOOLEAN) {
			validationInfo.error("operation '" + name + "' cannot be applied to '" + node.type.fullName + "'", node.node.getToken());
		}
		checkFinal(validationInfo, ctx, node.node != null ? node.node : node.resolvedValueVariable, true);
		return node.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.getOperationClass();
		if (c != TYPE_BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}
		v.bool = !v.bool;
	}
}
