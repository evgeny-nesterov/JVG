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

import static ru.nest.hiscript.ool.model.OperationType.*;

public class OperationLower extends BinaryOperation {
	private static final HiOperation instance = new OperationLower();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationLower() {
		super(LOWER);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
		HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
		if (c1.isNumber() && c2.isNumber()) {
			if (node1.isCompileValue() && node2.isCompileValue()) {
				PrimitiveType t1 = c1.getPrimitiveType();
				PrimitiveType t2 = c2.getPrimitiveType();
				switch (t1) {
					case CHAR_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.charValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.charValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.charValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.charValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.charValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.charValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.charValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case BYTE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.byteValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.byteValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.byteValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.byteValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.byteValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.byteValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.byteValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case SHORT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.shortValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.shortValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.shortValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.shortValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.shortValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.shortValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.shortValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case INT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.intValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.intValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.intValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.intValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.intValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.intValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.intValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case LONG_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.longValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.longValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.longValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.longValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.longValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.longValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.longValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case FLOAT_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.floatValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.floatValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.floatValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.floatValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.floatValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.floatValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.floatValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
					case DOUBLE_TYPE:
						switch (t2) {
							case CHAR_TYPE:
								node1.booleanValue = node1.doubleValue < node2.charValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case BYTE_TYPE:
								node1.booleanValue = node1.doubleValue < node2.byteValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case SHORT_TYPE:
								node1.booleanValue = node1.doubleValue < node2.shortValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case INT_TYPE:
								node1.booleanValue = node1.doubleValue < node2.intValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case LONG_TYPE:
								node1.booleanValue = node1.doubleValue < node2.longValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case FLOAT_TYPE:
								node1.booleanValue = node1.doubleValue < node2.floatValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
							case DOUBLE_TYPE:
								node1.booleanValue = node1.doubleValue < node2.doubleValue;
								return node1.valueClass = HiClassPrimitive.BOOLEAN;
						}
				}
			}
			return HiClassPrimitive.BOOLEAN;
		}
		errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			PrimitiveType t1 = c1.getPrimitiveType();
			PrimitiveType t2 = c2.getPrimitiveType();
			v1.valueClass = HiClassPrimitive.BOOLEAN;
			switch (t1) {
				case CHAR_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.character < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.character < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.character < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.character < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.character < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.character < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.character < v2.doubleNumber;
							return;
					}
				case BYTE_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.byteNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.byteNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.byteNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.byteNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.byteNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.byteNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.byteNumber < v2.doubleNumber;
							return;
					}
				case SHORT_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.shortNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.shortNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.shortNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.shortNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.shortNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.shortNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.shortNumber < v2.doubleNumber;
							return;
					}
				case INT_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.intNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.intNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.intNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.intNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.intNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.intNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.intNumber < v2.doubleNumber;
							return;
					}
				case LONG_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.longNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.longNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.longNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.longNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.longNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.longNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.longNumber < v2.doubleNumber;
							return;
					}
				case FLOAT_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.floatNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.floatNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.floatNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.floatNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.floatNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.floatNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.floatNumber < v2.doubleNumber;
							return;
					}
				case DOUBLE_TYPE:
					switch (t2) {
						case CHAR_TYPE:
							v1.bool = v1.doubleNumber < v2.character;
							return;
						case BYTE_TYPE:
							v1.bool = v1.doubleNumber < v2.byteNumber;
							return;
						case SHORT_TYPE:
							v1.bool = v1.doubleNumber < v2.shortNumber;
							return;
						case INT_TYPE:
							v1.bool = v1.doubleNumber < v2.intNumber;
							return;
						case LONG_TYPE:
							v1.bool = v1.doubleNumber < v2.longNumber;
							return;
						case FLOAT_TYPE:
							v1.bool = v1.doubleNumber < v2.floatNumber;
							return;
						case DOUBLE_TYPE:
							v1.bool = v1.doubleNumber < v2.doubleNumber;
							return;
					}
			}
		}
	}
}
