package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.OperationType.*;
import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationDivide extends BinaryOperation {
	private static final HiOperation instance = new OperationDivide();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationDivide() {
		super(DIVIDE);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (c1.isNumber() && c2.isNumber()) {
			PrimitiveType t1 = c1.getPrimitiveType();
			PrimitiveType t2 = c2.getPrimitiveType();
			boolean divisionByZero = false;
			if (node2.isCompileValue()) {
				switch (t2) {
					case CHAR_TYPE:
						if (node2.charValue == 0) {
							divisionByZero = true;
						}
						break;
					case BYTE_TYPE:
						if (node2.byteValue == 0) {
							divisionByZero = true;
						}
						break;
					case SHORT_TYPE:
						if (node2.shortValue == 0) {
							divisionByZero = true;
						}
						break;
					case INT_TYPE:
						if (node2.intValue == 0) {
							divisionByZero = true;
						}
						break;
					case LONG_TYPE:
						if (node2.longValue == 0) {
							divisionByZero = true;
						}
						break;
					case FLOAT_TYPE:
						if (node2.floatValue == 0) {
							divisionByZero = true;
						}
						break;
					case DOUBLE_TYPE:
						if (node2.doubleValue == 0) {
							divisionByZero = true;
						}
						break;
				}
			}

			if (node1.isCompileValue() && node2.isCompileValue() && divisionByZero) {
				// do not compute value during compilation
				node1.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
				node2.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
			}

			if (node1.isCompileValue() && node2.isCompileValue()) {
				switch (t1) {
					case CHAR_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.charValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.charValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.charValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.charValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.charValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								node1.floatValue = node1.charValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.charValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case BYTE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.byteValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.byteValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.byteValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.byteValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.byteValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								node1.floatValue = node1.byteValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.byteValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case SHORT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.shortValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.shortValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.shortValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.shortValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.shortValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								node1.floatValue = node1.shortValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.shortValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.intValue = node1.intValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE_TYPE:
								node1.intValue = node1.intValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT_TYPE:
								node1.intValue = node1.intValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT_TYPE:
								node1.intValue = node1.intValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG_TYPE:
								node1.longValue = node1.intValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								node1.floatValue = node1.intValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.intValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.longValue = node1.longValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case BYTE_TYPE:
								node1.longValue = node1.longValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case SHORT_TYPE:
								node1.longValue = node1.longValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case INT_TYPE:
								node1.longValue = node1.longValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case LONG_TYPE:
								node1.longValue = node1.longValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								node1.floatValue = node1.longValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.longValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case FLOAT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.floatValue = node1.floatValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case BYTE_TYPE:
								node1.floatValue = node1.floatValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case SHORT_TYPE:
								node1.floatValue = node1.floatValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case INT_TYPE:
								node1.floatValue = node1.floatValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case LONG_TYPE:
								node1.floatValue = node1.floatValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case FLOAT_TYPE:
								node1.floatValue = node1.floatValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.floatValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case DOUBLE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.doubleValue = node1.doubleValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case BYTE_TYPE:
								node1.doubleValue = node1.doubleValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case SHORT_TYPE:
								node1.doubleValue = node1.doubleValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case INT_TYPE:
								node1.doubleValue = node1.doubleValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case LONG_TYPE:
								node1.doubleValue = node1.doubleValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case FLOAT_TYPE:
								node1.doubleValue = node1.doubleValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case DOUBLE_TYPE:
								node1.doubleValue = node1.doubleValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
				}
			} else {
				switch (t1) {
					case CHAR_TYPE:
					case BYTE_TYPE:
					case SHORT_TYPE:
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
							case BYTE_TYPE:
							case SHORT_TYPE:
							case INT_TYPE:
								return HiClassPrimitive.INT;
							case LONG_TYPE:
								return HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								return HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								return HiClassPrimitive.DOUBLE;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
							case BYTE_TYPE:
							case SHORT_TYPE:
							case INT_TYPE:
							case LONG_TYPE:
								return HiClassPrimitive.LONG;
							case FLOAT_TYPE:
								return HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								return HiClassPrimitive.DOUBLE;
						}
					case FLOAT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
							case BYTE_TYPE:
							case SHORT_TYPE:
							case INT_TYPE:
							case LONG_TYPE:
							case FLOAT_TYPE:
								return HiClassPrimitive.FLOAT;
							case DOUBLE_TYPE:
								return HiClassPrimitive.DOUBLE;
						}
					case DOUBLE_TYPE:
						return HiClassPrimitive.DOUBLE;
				}
			}
		}
		errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		return null;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isNumber() && c2.isNumber()) {
			PrimitiveType t1 = c1.getPrimitiveType();
			PrimitiveType t2 = c2.getPrimitiveType();
			if (t1 != DOUBLE_TYPE && t1 != FLOAT_TYPE) {
				switch (t2) {
					case CHAR_TYPE:
						if (v2.character == 0) {
							errorDivideByZero(ctx);
							return;
						}
						break;
					case BYTE_TYPE:
						if (v2.byteNumber == 0) {
							errorDivideByZero(ctx);
							return;
						}
						break;
					case SHORT_TYPE:
						if (v2.shortNumber == 0) {
							errorDivideByZero(ctx);
							return;
						}
						break;
					case INT_TYPE:
						if (v2.intNumber == 0) {
							errorDivideByZero(ctx);
							return;
						}
						break;
					case LONG_TYPE:
						if (v2.longNumber == 0) {
							errorDivideByZero(ctx);
							return;
						}
						break;
				}
			}

			if (v1.valueType == ValueType.VALUE && v2.valueType == ValueType.VALUE) {
				switch (t1) {
					case CHAR_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								autoCastInt(v1, v1.character / v2.character);
								return;
							case BYTE_TYPE:
								autoCastInt(v1, v1.character / v2.byteNumber);
								return;
							case SHORT_TYPE:
								autoCastInt(v1, v1.character / v2.shortNumber);
								return;
							case INT_TYPE:
								autoCastInt(v1, v1.character / v2.intNumber);
								return;
							case LONG_TYPE:
								autoCastLong(v1, v1.character / v2.longNumber);
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.character / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.character / v2.doubleNumber;
								return;
						}
					case BYTE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								autoCastInt(v1, v1.byteNumber / v2.character);
								return;
							case BYTE_TYPE:
								autoCastInt(v1, v1.byteNumber / v2.byteNumber);
								return;
							case SHORT_TYPE:
								autoCastInt(v1, v1.byteNumber / v2.shortNumber);
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								autoCastInt(v1, v1.byteNumber / v2.intNumber);
								return;
							case LONG_TYPE:
								autoCastLong(v1, v1.byteNumber / v2.longNumber);
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.byteNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.byteNumber / v2.doubleNumber;
								return;
						}
					case SHORT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								autoCastInt(v1, v1.shortNumber / v2.character);
								return;
							case BYTE_TYPE:
								autoCastInt(v1, v1.shortNumber / v2.byteNumber);
								return;
							case SHORT_TYPE:
								autoCastInt(v1, v1.shortNumber / v2.shortNumber);
								return;
							case INT_TYPE:
								autoCastInt(v1, v1.shortNumber / v2.intNumber);
								return;
							case LONG_TYPE:
								autoCastLong(v1, v1.shortNumber / v2.longNumber);
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.shortNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.shortNumber / v2.doubleNumber;
								return;
						}
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								autoCastInt(v1, v1.intNumber / v2.character);
								return;
							case BYTE_TYPE:
								autoCastInt(v1, v1.intNumber / v2.byteNumber);
								return;
							case SHORT_TYPE:
								autoCastInt(v1, v1.intNumber / v2.shortNumber);
								return;
							case INT_TYPE:
								autoCastInt(v1, v1.intNumber / v2.intNumber);
								return;
							case LONG_TYPE:
								autoCastLong(v1, v1.intNumber / v2.longNumber);
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.intNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.intNumber / v2.doubleNumber;
								return;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								autoCastLong(v1, v1.longNumber / v2.character);
								return;
							case BYTE_TYPE:
								autoCastLong(v1, v1.longNumber / v2.byteNumber);
								return;
							case SHORT_TYPE:
								autoCastLong(v1, v1.longNumber / v2.shortNumber);
								return;
							case INT_TYPE:
								autoCastLong(v1, v1.longNumber / v2.intNumber);
								return;
							case LONG_TYPE:
								autoCastLong(v1, v1.longNumber / v2.longNumber);
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.longNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.longNumber / v2.doubleNumber;
								return;
						}
					case FLOAT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.floatNumber / v2.doubleNumber;
								return;
						}
					case DOUBLE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.doubleNumber;
								return;
						}
				}
			} else {
				switch (t1) {
					case CHAR_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.character / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.character / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.character / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.character / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.character / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.character / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.character / v2.doubleNumber;
								return;
						}
					case BYTE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.intNumber = v1.byteNumber / v2.character;
								v1.valueClass = HiClassPrimitive.INT;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.byteNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.byteNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.byteNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.byteNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.byteNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.byteNumber / v2.doubleNumber;
								return;
						}
					case SHORT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.shortNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.shortNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.shortNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.shortNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.shortNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.shortNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.shortNumber / v2.doubleNumber;
								return;
						}
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.intNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.intNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.intNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.INT;
								v1.intNumber = v1.intNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.intNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.intNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.intNumber / v2.doubleNumber;
								return;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.longNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.longNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.longNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.longNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.LONG;
								v1.longNumber = v1.longNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.longNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.longNumber / v2.doubleNumber;
								return;
						}
					case FLOAT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.FLOAT;
								v1.floatNumber = v1.floatNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.floatNumber / v2.doubleNumber;
								return;
						}
					case DOUBLE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.character;
								return;
							case BYTE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.byteNumber;
								return;
							case SHORT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.shortNumber;
								return;
							case INT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.intNumber;
								return;
							case LONG_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.longNumber;
								return;
							case FLOAT_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.floatNumber;
								return;
							case DOUBLE_TYPE:
								v1.valueClass = HiClassPrimitive.DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.doubleNumber;
								return;
						}
				}
			}
		}
	}
}
