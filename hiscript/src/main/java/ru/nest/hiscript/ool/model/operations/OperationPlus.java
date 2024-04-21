package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationPlus extends BinaryOperation {
	private static final HiOperation instance = new OperationPlus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationPlus() {
		super(PLUS);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();

		boolean isS1 = c1 != null && HiClass.STRING_CLASS_NAME.equals(c1.fullName);
		boolean isS2 = c2 != null && HiClass.STRING_CLASS_NAME.equals(c2.fullName);
		if (isS1 || isS2) {
			return isS1 ? c1 : c2;
		}

		if (!c1.isNumber() || !c2.isNumber()) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}

		int t1 = c1.getPrimitiveType();
		int t2 = c2.getPrimitiveType();
		if (node1.isCompileValue() && node2.isCompileValue()) {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.charValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.charValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.charValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.charValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.charValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.charValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.charValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case BYTE:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.byteValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.byteValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.byteValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.byteValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.byteValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.byteValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.byteValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case SHORT:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.shortValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.shortValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.shortValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.shortValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.shortValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.shortValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.shortValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case INT:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.intValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.intValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.intValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.intValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.intValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.intValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.intValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case LONG:
					switch (t2) {
						case CHAR:
							node1.longValue = node1.longValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.longValue = node1.longValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.longValue = node1.longValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.longValue = node1.longValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.longValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.longValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.longValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case FLOAT:
					switch (t2) {
						case CHAR:
							node1.floatValue = node1.floatValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.floatValue = node1.floatValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.floatValue = node1.floatValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.floatValue = node1.floatValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.floatValue = node1.floatValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.floatValue = node1.floatValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.floatValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
				case DOUBLE:
					switch (t2) {
						case CHAR:
							node1.doubleValue = node1.doubleValue + node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.doubleValue = node1.doubleValue + node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.doubleValue = node1.doubleValue + node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.doubleValue = node1.doubleValue + node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.doubleValue = node1.doubleValue + node2.longValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case FLOAT:
							node1.doubleValue = node1.doubleValue + node2.floatValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case DOUBLE:
							node1.doubleValue = node1.doubleValue + node2.doubleValue;
							return node1.valueClass = HiClassPrimitive.INT;
					}
			}
		} else {
			if (t1 == VAR) {
				return c2;
			} else if (t2 == VAR) {
				return c1;
			}
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
					break;

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
					break;

				case DOUBLE:
					return HiClassPrimitive.DOUBLE;
			}
		}

		errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		return null;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();

		boolean isS1 = c1 != null && HiClass.STRING_CLASS_NAME.equals(c1.fullName);
		boolean isS2 = c2 != null && HiClass.STRING_CLASS_NAME.equals(c2.fullName);
		if (isS1 || isS2) {
			char[] chars1 = v1.getString(ctx);
			char[] chars2 = v2.getString(ctx);
			char[] chars = new char[chars1.length + chars2.length];
			System.arraycopy(chars1, 0, chars, 0, chars1.length);
			System.arraycopy(chars2, 0, chars, chars1.length, chars2.length);
			NodeString.createString(ctx, chars);

			v1.valueClass = ctx.value.valueClass;
			v1.lambdaClass = null;
			v1.object = ctx.value.object;
			return;
		}

		if (c1.isNumber() && c2.isNumber()) {
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			if (v1.valueType == Value.VALUE && v2.valueType == Value.VALUE) {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.character + v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.character + v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.character + v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.character + v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.character + v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.character + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.character + v2.doubleNumber;
								return;
						}
						break;

					case BYTE:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.byteNumber + v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.byteNumber + v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.byteNumber + v2.shortNumber);
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								autoCastInt(v1, v1.byteNumber + v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.byteNumber + v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.byteNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.byteNumber + v2.doubleNumber;
								return;
						}
						break;

					case SHORT:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.shortNumber + v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.shortNumber + v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.shortNumber + v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.shortNumber + v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.shortNumber + v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.shortNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.shortNumber + v2.doubleNumber;
								return;
						}
						break;

					case INT:
						switch (t2) {
							case CHAR:
								autoCastInt(v1, v1.intNumber + v2.character);
								return;
							case BYTE:
								autoCastInt(v1, v1.intNumber + v2.byteNumber);
								return;
							case SHORT:
								autoCastInt(v1, v1.intNumber + v2.shortNumber);
								return;
							case INT:
								autoCastInt(v1, v1.intNumber + v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.intNumber + v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.intNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.intNumber + v2.doubleNumber;
								return;
						}
						break;

					case LONG:
						switch (t2) {
							case CHAR:
								autoCastLong(v1, v1.longNumber + v2.character);
								return;
							case BYTE:
								autoCastLong(v1, v1.longNumber + v2.byteNumber);
								return;
							case SHORT:
								autoCastLong(v1, v1.longNumber + v2.shortNumber);
								return;
							case INT:
								autoCastLong(v1, v1.longNumber + v2.intNumber);
								return;
							case LONG:
								autoCastLong(v1, v1.longNumber + v2.longNumber);
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.longNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.longNumber + v2.doubleNumber;
								return;
						}
						break;

					case FLOAT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.floatNumber + v2.doubleNumber;
								return;
						}
						break;

					case DOUBLE:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.doubleNumber;
								return;
						}
						break;
				}
			} else {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.character + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.character + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.character + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.character + v2.doubleNumber;
								return;
						}
						break;

					case BYTE:
						switch (t2) {
							case CHAR:
								v1.intNumber = v1.byteNumber + v2.character;
								v1.valueClass = TYPE_INT;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.byteNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.byteNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.byteNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.byteNumber + v2.doubleNumber;
								return;
						}
						break;

					case SHORT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.shortNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.shortNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.shortNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.shortNumber + v2.doubleNumber;
								return;
						}
						break;

					case INT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_INT;
								v1.intNumber = v1.intNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.intNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.intNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.intNumber + v2.doubleNumber;
								return;
						}
						break;

					case LONG:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_LONG;
								v1.longNumber = v1.longNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.longNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.longNumber + v2.doubleNumber;
								return;
						}
						break;

					case FLOAT:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_FLOAT;
								v1.floatNumber = v1.floatNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.floatNumber + v2.doubleNumber;
								return;
						}
						break;

					case DOUBLE:
						switch (t2) {
							case CHAR:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.character;
								return;
							case BYTE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.byteNumber;
								return;
							case SHORT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.shortNumber;
								return;
							case INT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.intNumber;
								return;
							case LONG:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.longNumber;
								return;
							case FLOAT:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.floatNumber;
								return;
							case DOUBLE:
								v1.valueClass = TYPE_DOUBLE;
								v1.doubleNumber = v1.doubleNumber + v2.doubleNumber;
								return;
						}
						break;
				}
			}
		}

		errorInvalidOperator(ctx, v1.valueClass, v2.valueClass);
	}
}
