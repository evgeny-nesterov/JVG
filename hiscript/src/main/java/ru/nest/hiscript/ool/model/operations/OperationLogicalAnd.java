package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import static ru.nest.hiscript.ool.model.OperationType.*;

public class OperationLogicalAnd extends BinaryOperation {
	private static final HiOperation instance = new OperationLogicalAnd();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationLogicalAnd() {
		super(LOGICAL_AND);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (c1 == HiClassPrimitive.BOOLEAN && c2 == HiClassPrimitive.BOOLEAN) {
			if (node1.isCompileValue() && node2.isCompileValue()) {
				node1.booleanValue = node1.booleanValue && node2.booleanValue;
			}
			return HiClassPrimitive.BOOLEAN;
		} else {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		v1.unbox();
		v2.unbox();
		v1.valueClass = HiClassPrimitive.BOOLEAN;
		v1.bool = v1.bool && v2.bool;
	}
}
