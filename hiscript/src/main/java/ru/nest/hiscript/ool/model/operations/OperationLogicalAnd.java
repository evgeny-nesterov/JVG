package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationLogicalAnd extends BinaryOperation {
	private static HiOperation instance = new OperationLogicalAnd();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationLogicalAnd() {
		super("&&", LOGICAL_AND);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (c1 == HiClassPrimitive.BOOLEAN && c2 == HiClassPrimitive.BOOLEAN) {
			return HiClassPrimitive.BOOLEAN;
		} else {
			errorInvalidOperator(validationInfo, node1.node.getToken(), c1, c2);
			return null;
		}
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;
		if (c1 == TYPE_BOOLEAN && c2 == TYPE_BOOLEAN) {
			v1.bool = v1.bool && v2.bool;
		} else {
			errorInvalidOperator(ctx, c1, c2);
		}
	}
}
