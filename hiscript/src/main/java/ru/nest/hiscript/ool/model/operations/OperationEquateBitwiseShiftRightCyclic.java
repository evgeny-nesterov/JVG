package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.OperationType.*;

public class OperationEquateBitwiseShiftRightCyclic extends BinaryOperation {
	private static final HiOperation instance = new OperationEquateBitwiseShiftRightCyclic();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquateBitwiseShiftRightCyclic() {
		super(EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC);
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		if (!node1.isVariable()) {
			validationInfo.error("variable expected", node1.token);
		}

		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (!c1.isPrimitive() || !c2.isPrimitive()) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}
		if (c1 == HiClassPrimitive.BOOLEAN || c1 == HiClassPrimitive.FLOAT || c1 == HiClassPrimitive.DOUBLE ||
				c2 == HiClassPrimitive.BOOLEAN || c2 == HiClassPrimitive.FLOAT || c2 == HiClassPrimitive.DOUBLE) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}
		if (node1.clazz == HiClassPrimitive.BYTE.getAutoboxClass() || node1.clazz == HiClassPrimitive.SHORT.getAutoboxClass() || node1.clazz == HiClassPrimitive.CHAR.getAutoboxClass()) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}

		HiNodeIF node = node1.node != null ? node1.node : node1.resolvedValueVariable;
		checkFinal(validationInfo, ctx, node, true);
		return node1.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		PrimitiveType t1 = c1.getPrimitiveType();
		PrimitiveType t2 = c2.getPrimitiveType();
		switch (t1) {
			case CHAR_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.character >>>= v2.character;
						break;
					case BYTE_TYPE:
						v1.character >>>= v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.character >>>= v2.shortNumber;
						break;
					case INT_TYPE:
						v1.character >>>= v2.intNumber;
						break;
					case LONG_TYPE:
						v1.character >>>= v2.longNumber;
						break;
				}
				break;
			case BYTE_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.byteNumber >>>= v2.character;
						break;
					case BYTE_TYPE:
						v1.byteNumber >>>= v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.byteNumber >>>= v2.shortNumber;
						break;
					case INT_TYPE:
						v1.byteNumber >>>= v2.intNumber;
						break;
					case LONG_TYPE:
						v1.byteNumber >>>= v2.longNumber;
						break;
				}
				break;
			case SHORT_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.shortNumber >>>= v2.character;
						break;
					case BYTE_TYPE:
						v1.shortNumber >>>= v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.shortNumber >>>= v2.shortNumber;
						break;
					case INT_TYPE:
						v1.shortNumber >>>= v2.intNumber;
						break;
					case LONG_TYPE:
						v1.shortNumber >>>= v2.longNumber;
						break;
				}
				break;
			case INT_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.intNumber >>>= v2.character;
						break;
					case BYTE_TYPE:
						v1.intNumber >>>= v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.intNumber >>>= v2.shortNumber;
						break;
					case INT_TYPE:
						v1.intNumber >>>= v2.intNumber;
						break;
					case LONG_TYPE:
						v1.intNumber >>>= v2.longNumber;
						break;
				}
				break;
			case LONG_TYPE:
				switch (t2) {
					case CHAR_TYPE:
						v1.longNumber >>>= v2.character;
						break;
					case BYTE_TYPE:
						v1.longNumber >>>= v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.longNumber >>>= v2.shortNumber;
						break;
					case INT_TYPE:
						v1.longNumber >>>= v2.intNumber;
						break;
					case LONG_TYPE:
						v1.longNumber >>>= v2.longNumber;
						break;
				}
				break;
		}

		// @autoboxing
		if (v1.valueClass.isObject()) {
			v1.valueClass = c1;
			v1.object = ((HiClassPrimitive) c1).box(ctx, v1);
		}

		if (v1.valueType == ValueType.VARIABLE) {
			v1.variable.set(ctx, v1);
		} else if (v1.valueType == ValueType.ARRAY_INDEX) {
			v1.copyToArray(v1);
		}
	}
}
