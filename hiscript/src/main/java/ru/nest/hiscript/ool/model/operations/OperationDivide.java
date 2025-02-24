package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

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
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			boolean divisionByZero = false;
			if (node2.isCompileValue()) {
				switch (t2) {
					case CHAR:
						if (node2.charValue == 0) {
							divisionByZero = true;
						}
						break;
					case BYTE:
						if (node2.byteValue == 0) {
							divisionByZero = true;
						}
						break;
					case SHORT:
						if (node2.shortValue == 0) {
							divisionByZero = true;
						}
						break;
					case INT:
						if (node2.intValue == 0) {
							divisionByZero = true;
						}
						break;
					case LONG:
						if (node2.longValue == 0) {
							divisionByZero = true;
						}
						break;
					case FLOAT:
						if (node2.floatValue == 0) {
							divisionByZero = true;
						}
						break;
					case DOUBLE:
						if (node2.doubleValue == 0) {
							divisionByZero = true;
						}
						break;
				}
			}

			if (node1.isCompileValue() && node2.isCompileValue()) {
				if (divisionByZero && !(t1 == FLOAT || t1 == DOUBLE || t2 == FLOAT || t2 == DOUBLE)) {
					// do not compute value during compilation
					node1.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
					node2.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
				}
			}

			if (node1.isCompileValue() && node2.isCompileValue()) {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								node1.intValue = node1.charValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE:
								node1.intValue = node1.charValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT:
								node1.intValue = node1.charValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT:
								node1.intValue = node1.charValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG:
								node1.longValue = node1.charValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT:
								node1.floatValue = node1.charValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.charValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case BYTE:
						switch (t2) {
							case CHAR:
								node1.intValue = node1.byteValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE:
								node1.intValue = node1.byteValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT:
								node1.intValue = node1.byteValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT:
								node1.intValue = node1.byteValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG:
								node1.longValue = node1.byteValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT:
								node1.floatValue = node1.byteValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.byteValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case SHORT:
						switch (t2) {
							case CHAR:
								node1.intValue = node1.shortValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE:
								node1.intValue = node1.shortValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT:
								node1.intValue = node1.shortValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT:
								node1.intValue = node1.shortValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG:
								node1.longValue = node1.shortValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT:
								node1.floatValue = node1.shortValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.shortValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case INT:
						switch (t2) {
							case CHAR:
								node1.intValue = node1.intValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case BYTE:
								node1.intValue = node1.intValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case SHORT:
								node1.intValue = node1.intValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case INT:
								node1.intValue = node1.intValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case LONG:
								node1.longValue = node1.intValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT:
								node1.floatValue = node1.intValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.intValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case LONG:
						switch (t2) {
							case CHAR:
								node1.longValue = node1.longValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case BYTE:
								node1.longValue = node1.longValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case SHORT:
								node1.longValue = node1.longValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case INT:
								node1.longValue = node1.longValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case LONG:
								node1.longValue = node1.longValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.LONG;
							case FLOAT:
								node1.floatValue = node1.longValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.longValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case FLOAT:
						switch (t2) {
							case CHAR:
								node1.floatValue = node1.floatValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case BYTE:
								node1.floatValue = node1.floatValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case SHORT:
								node1.floatValue = node1.floatValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case INT:
								node1.floatValue = node1.floatValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case LONG:
								node1.floatValue = node1.floatValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.INT;
							case FLOAT:
								node1.floatValue = node1.floatValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.FLOAT;
							case DOUBLE:
								node1.doubleValue = node1.floatValue / node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
					case DOUBLE:
						switch (t2) {
							case CHAR:
								node1.doubleValue = node1.doubleValue / node2.charValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case BYTE:
								node1.doubleValue = node1.doubleValue / node2.byteValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case SHORT:
								node1.doubleValue = node1.doubleValue / node2.shortValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case INT:
								node1.doubleValue = node1.doubleValue / node2.intValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case LONG:
								node1.doubleValue = node1.doubleValue / node2.longValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case FLOAT:
								node1.doubleValue = node1.doubleValue / node2.floatValue;
								return node1.valueClass = HiClassPrimitive.DOUBLE;
							case DOUBLE:
								return node1.valueClass = HiClassPrimitive.DOUBLE;
						}
				}
			} else {
				switch (t1) {
					case CHAR:
					case BYTE:
					case SHORT:
					case INT:
						switch (t2) {
							case CHAR:
							case BYTE:
							case SHORT:
							case INT:
								return HiClassPrimitive.INT;
							case LONG:
								return HiClassPrimitive.LONG;
							case FLOAT:
								return HiClassPrimitive.FLOAT;
							case DOUBLE:
								return HiClassPrimitive.DOUBLE;
						}
					case LONG:
						switch (t2) {
							case CHAR:
							case BYTE:
							case SHORT:
							case INT:
							case LONG:
								return HiClassPrimitive.LONG;
							case FLOAT:
								return HiClassPrimitive.FLOAT;
							case DOUBLE:
								return HiClassPrimitive.DOUBLE;
						}
					case FLOAT:
						switch (t2) {
							case CHAR:
							case BYTE:
							case SHORT:
							case INT:
							case LONG:
							case FLOAT:
								return HiClassPrimitive.FLOAT;
							case DOUBLE:
								return HiClassPrimitive.DOUBLE;
						}
					case DOUBLE:
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
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			switch (t2) {
				case CHAR:
					if (v2.character == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case BYTE:
					if (v2.byteNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case SHORT:
					if (v2.shortNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case INT:
					if (v2.intNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case LONG:
					if (v2.longNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case FLOAT:
					if (v2.floatNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
				case DOUBLE:
					if (v2.doubleNumber == 0) {
						errorDivideByZero(ctx);
						return;
					}
					break;
			}

			if (v1.valueType == Value.VALUE && v2.valueType == Value.VALUE) {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.character / v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.character / v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.character / v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.character / v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.character / v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.character / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.character / v2.doubleNumber;
								return;
						}
					case BYTE:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.byteNumber / v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.byteNumber / v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.byteNumber / v2.shortNumber);
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								autoCastInt(v1, v1.byteNumber / v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.byteNumber / v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.byteNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.byteNumber / v2.doubleNumber;
								return;
						}
					case SHORT:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.shortNumber / v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.shortNumber / v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.shortNumber / v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.shortNumber / v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.shortNumber / v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.shortNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.shortNumber / v2.doubleNumber;
								return;
						}
					case INT:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.intNumber / v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.intNumber / v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.intNumber / v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.intNumber / v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.intNumber / v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.intNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.intNumber / v2.doubleNumber;
								return;
						}
					case LONG:
						switch (t2) {
							case CHAR:
								autoCastLong(v1, v1.longNumber / v2.character);
								return;
							case BYTE:
								autoCastLong(v1, v1.longNumber / v2.byteNumber);
								return;
							case SHORT:
								autoCastLong(v1, v1.longNumber / v2.shortNumber);
								return;
							case INT:
								autoCastLong(v1, v1.longNumber / v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.longNumber / v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.longNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.longNumber / v2.doubleNumber;
								return;
						}
					case FLOAT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.floatNumber / v2.doubleNumber;
								return;
						}
					case DOUBLE:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.doubleNumber;
								return;
						}
				}
			} else {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.character / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.character / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.character / v2.doubleNumber;
								return;
						}
					case BYTE:
						switch (t2) {
							case CHAR:
								v1.intNumber = v1.byteNumber / v2.character;
								v1.valueClass = TYPE_INT;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.byteNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.byteNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.byteNumber / v2.doubleNumber;
								return;
						}
					case SHORT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.shortNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.shortNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.shortNumber / v2.doubleNumber;
								return;
						}
					case INT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.intNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.intNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.intNumber / v2.doubleNumber;
								return;
						}
					case LONG:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.longNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.longNumber / v2.doubleNumber;
								return;
						}
					case FLOAT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.floatNumber / v2.doubleNumber;
								return;
						}
					case DOUBLE:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber / v2.doubleNumber;
								return;
						}
				}
			}
		}
	}
}
