package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

public class OperationXOR extends BinaryOperation {
	private static final HiOperation instance = new OperationXOR();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationXOR() {
		super(XOR);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (!c1.isPrimitive() || !c2.isPrimitive()) {
			errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
			return null;
		}

		int t1 = c1.getPrimitiveType();
		int t2 = c2.getPrimitiveType();
		if (t1 == BOOLEAN || t2 == BOOLEAN) {
			if (t1 == BOOLEAN && t2 == BOOLEAN) {
				if (node1.isCompileValue() && node2.isCompileValue()) {
					node1.booleanValue = node1.booleanValue ^ node2.booleanValue;
					return node1.valueClass = TYPE_BOOLEAN;
				}
				return HiClassPrimitive.BOOLEAN;
			}
		} else if (node1.isCompileValue() && node2.isCompileValue()) {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.charValue ^ node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.charValue ^ node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.charValue ^ node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.charValue ^ node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.charValue ^ node2.longValue;
							return node1.valueClass = HiClassPrimitive.LONG;
					}
				case BYTE:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.byteValue ^ node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.byteValue ^ node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.byteValue ^ node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.byteValue ^ node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.byteValue ^ node2.longValue;
							return node1.valueClass = HiClassPrimitive.LONG;
					}
				case SHORT:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.shortValue ^ node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.shortValue ^ node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.shortValue ^ node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.shortValue ^ node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.shortValue ^ node2.longValue;
							return node1.valueClass = HiClassPrimitive.LONG;
					}
				case INT:
					switch (t2) {
						case CHAR:
							node1.intValue = node1.intValue ^ node2.charValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case BYTE:
							node1.intValue = node1.intValue ^ node2.byteValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case SHORT:
							node1.intValue = node1.intValue ^ node2.shortValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case INT:
							node1.intValue = node1.intValue ^ node2.intValue;
							return node1.valueClass = HiClassPrimitive.INT;
						case LONG:
							node1.longValue = node1.intValue ^ node2.longValue;
							return node1.valueClass = HiClassPrimitive.LONG;
					}
				case LONG:
					switch (t2) {
						case CHAR:
							node1.longValue = node1.longValue ^ node2.charValue;
							return node1.valueClass = HiClassPrimitive.LONG;
						case BYTE:
							node1.longValue = node1.longValue ^ node2.byteValue;
							return node1.valueClass = HiClassPrimitive.LONG;
						case SHORT:
							node1.longValue = node1.longValue ^ node2.shortValue;
							return node1.valueClass = HiClassPrimitive.LONG;
						case INT:
							node1.longValue = node1.longValue ^ node2.intValue;
							return node1.valueClass = HiClassPrimitive.LONG;
						case LONG:
							node1.longValue = node1.longValue ^ node2.longValue;
							return node1.valueClass = HiClassPrimitive.LONG;
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
					}
				case LONG:
					switch (t2) {
						case CHAR:
						case BYTE:
						case SHORT:
						case INT:
						case LONG:
							return HiClassPrimitive.LONG;
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
		int t1 = c1.getPrimitiveType();
		int t2 = c2.getPrimitiveType();
		if (t1 == BOOLEAN || t2 == BOOLEAN) {
			if (t1 == BOOLEAN && t2 == BOOLEAN) {
				v1.valueClass = TYPE_BOOLEAN;
				v1.bool = v1.bool ^ v2.bool;
			}
		} else if (v1.valueType == ValueType.VALUE && v2.valueType == ValueType.VALUE) {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.character ^ v2.character);
							return;
						case BYTE:
							autoCastInt(v1, v1.character ^ v2.byteNumber);
							return;
						case SHORT:
							autoCastInt(v1, v1.character ^ v2.shortNumber);
							return;
						case INT:
							autoCastInt(v1, v1.character ^ v2.intNumber);
							return;
						case LONG:
							autoCastLong(v1, v1.character ^ v2.longNumber);
							return;
					}
				case BYTE:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.byteNumber ^ v2.character);
							return;
						case BYTE:
							autoCastInt(v1, v1.byteNumber ^ v2.byteNumber);
							return;
						case SHORT:
							autoCastInt(v1, v1.byteNumber ^ v2.shortNumber);
							return;
						case INT:
							v1.valueClass = TYPE_INT;
							autoCastInt(v1, v1.byteNumber ^ v2.intNumber);
							return;
						case LONG:
							autoCastLong(v1, v1.byteNumber ^ v2.longNumber);
							return;
					}
				case SHORT:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.shortNumber ^ v2.character);
							return;
						case BYTE:
							autoCastInt(v1, v1.shortNumber ^ v2.byteNumber);
							return;
						case SHORT:
							autoCastInt(v1, v1.shortNumber ^ v2.shortNumber);
							return;
						case INT:
							autoCastInt(v1, v1.shortNumber ^ v2.intNumber);
							return;
						case LONG:
							autoCastLong(v1, v1.shortNumber ^ v2.longNumber);
							return;
					}
				case INT:
					switch (t2) {
						case CHAR:
							autoCastInt(v1, v1.intNumber ^ v2.character);
							return;
						case BYTE:
							autoCastInt(v1, v1.intNumber ^ v2.byteNumber);
							return;
						case SHORT:
							autoCastInt(v1, v1.intNumber ^ v2.shortNumber);
							return;
						case INT:
							autoCastInt(v1, v1.intNumber ^ v2.intNumber);
							return;
						case LONG:
							autoCastLong(v1, v1.intNumber ^ v2.longNumber);
							return;
					}
				case LONG:
					switch (t2) {
						case CHAR:
							autoCastLong(v1, v1.longNumber ^ v2.character);
							return;
						case BYTE:
							autoCastLong(v1, v1.longNumber ^ v2.byteNumber);
							return;
						case SHORT:
							autoCastLong(v1, v1.longNumber ^ v2.shortNumber);
							return;
						case INT:
							autoCastLong(v1, v1.longNumber ^ v2.intNumber);
							return;
						case LONG:
							autoCastLong(v1, v1.longNumber ^ v2.longNumber);
							return;
					}
			}
		} else {
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.character ^ v2.character;
							return;
						case BYTE:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.character ^ v2.byteNumber;
							return;
						case SHORT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.character ^ v2.shortNumber;
							return;
						case INT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.character ^ v2.intNumber;
							return;
						case LONG:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.character ^ v2.longNumber;
							return;
					}
				case BYTE:
					switch (t2) {
						case CHAR:
							v1.intNumber = v1.byteNumber ^ v2.character;
							v1.valueClass = TYPE_INT;
							return;
						case BYTE:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.byteNumber ^ v2.byteNumber;
							return;
						case SHORT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.byteNumber ^ v2.shortNumber;
							return;
						case INT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.byteNumber ^ v2.intNumber;
							return;
						case LONG:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.byteNumber ^ v2.longNumber;
							return;
					}
				case SHORT:
					switch (t2) {
						case CHAR:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.shortNumber ^ v2.character;
							return;
						case BYTE:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.shortNumber ^ v2.byteNumber;
							return;
						case SHORT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.shortNumber ^ v2.shortNumber;
							return;
						case INT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.shortNumber ^ v2.intNumber;
							return;
						case LONG:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.shortNumber ^ v2.longNumber;
							return;
					}
				case INT:
					switch (t2) {
						case CHAR:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.intNumber ^ v2.character;
							return;
						case BYTE:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.intNumber ^ v2.byteNumber;
							return;
						case SHORT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.intNumber ^ v2.shortNumber;
							return;
						case INT:
							v1.valueClass = TYPE_INT;
							v1.intNumber = v1.intNumber ^ v2.intNumber;
							return;
						case LONG:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.intNumber ^ v2.longNumber;
							return;
					}
				case LONG:
					switch (t2) {
						case CHAR:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.longNumber ^ v2.character;
							return;
						case BYTE:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.longNumber ^ v2.byteNumber;
							return;
						case SHORT:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.longNumber ^ v2.shortNumber;
							return;
						case INT:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.longNumber ^ v2.intNumber;
							return;
						case LONG:
							v1.valueClass = TYPE_LONG;
							v1.longNumber = v1.longNumber ^ v2.longNumber;
							return;
					}
			}
		}
	}
}
