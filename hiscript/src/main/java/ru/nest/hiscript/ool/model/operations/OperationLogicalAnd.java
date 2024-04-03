package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationLogicalAnd extends BinaryOperation {
	private static final HiOperation instance = new OperationLogicalAnd();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationLogicalAnd() {
		super("&&", LOGICAL_AND);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.type.getAutoboxedPrimitiveClass() == null ? node1.type : node1.type.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.type.getAutoboxedPrimitiveClass() == null ? node2.type : node2.type.getAutoboxedPrimitiveClass();
		if ((c1 == HiClassPrimitive.BOOLEAN || c1.isVar()) && (c2 == HiClassPrimitive.BOOLEAN || c2.isVar())) {
			return HiClassPrimitive.BOOLEAN;
		} else {
			errorInvalidOperator(validationInfo, node1.token, node1.type, node2.type);
			return null;
		}
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1 == TYPE_BOOLEAN && c2 == TYPE_BOOLEAN) {
			v1.type = TYPE_BOOLEAN;
			v1.bool = v1.bool && v2.bool;
		} else {
			errorInvalidOperator(ctx, v1.type, v2.type);
		}
	}
}
