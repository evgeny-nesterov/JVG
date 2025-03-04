package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.OperationType.*;
import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationCast extends BinaryOperation {
	private static final HiOperation instance = new OperationCast();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationCast() {
		super(CAST);
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		HiClass c1 = node1.clazz;
		HiClass c2 = node2.clazz;
		if (c1.isPrimitive()) {
			if (c2 == HiClass.NUMBER_CLASS) {
				if (!c1.isNumber()) {
					errorCast(validationInfo, node1.token, c2, c1);
				}
			} else if (!c2.isPrimitive()) {
				if (c2.getAutoboxedPrimitiveClass() != c1) {
					errorCast(validationInfo, node1.token, c2, c1);
				}
			} else {
				if ((c1.getPrimitiveType() == BOOLEAN_TYPE && c2.getPrimitiveType() != BOOLEAN_TYPE) || (c1.getPrimitiveType() != BOOLEAN_TYPE && c2.getPrimitiveType() == BOOLEAN_TYPE)) {
					errorCast(validationInfo, node1.token, c2, c1);
				}
			}
		} else if (c1.isArray() && c2.isArray()) {
			// c1 and c2 has to be in one hierarchy path
			HiClassArray ca1 = (HiClassArray) c1;
			HiClassArray ca2 = (HiClassArray) c2;
			if (!canCastArray(ca1, ca2) && !canCastArray(ca2, ca1)) {
				errorInconvertible(validationInfo, node1.token, c2, c1);
			}
		} else {
			// c1 and c2 has to be in one hierarchy path
			if (!c2.isNull() && !c2.isInstanceof(c1) && !c1.isInstanceof(c2)) {
				errorCast(validationInfo, node1.token, c2, c1);
			}
		}
		if (node2.isCompileValue() && (c1.isPrimitive() || c1 == HiClass.STRING_CLASS)) {
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.compileValue;
			node1.valueClass = c1;
			if (c1.isPrimitive() && c2.isPrimitive()) {
				PrimitiveType t1 = c1.getPrimitiveType();
				PrimitiveType t2 = c2.getPrimitiveType();
				switch (t1) {
					case BOOLEAN_TYPE:
						if (t2 == BOOLEAN_TYPE) {
							node1.booleanValue = node2.booleanValue;
						}
						break;
					case BYTE_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.byteValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.byteValue = (byte) node2.shortValue;
								break;
							case INT_TYPE:
								node1.byteValue = (byte) node2.intValue;
								break;
							case LONG_TYPE:
								node1.byteValue = (byte) node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.byteValue = (byte) node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.byteValue = (byte) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.byteValue = (byte) node2.charValue;
								break;
						}
						break;
					case SHORT_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.shortValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.shortValue = node2.shortValue;
								break;
							case INT_TYPE:
								node1.shortValue = (short) node2.intValue;
								break;
							case LONG_TYPE:
								node1.shortValue = (short) node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.shortValue = (short) node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.shortValue = (short) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.shortValue = (short) node2.charValue;
								break;
						}
						break;
					case INT_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.intValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.intValue = node2.shortValue;
								break;
							case INT_TYPE:
								node1.intValue = node2.intValue;
								break;
							case LONG_TYPE:
								node1.intValue = (int) node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.intValue = (int) node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.intValue = (int) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.intValue = node2.charValue;
								break;
						}
						break;
					case LONG_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.longValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.longValue = node2.shortValue;
								break;
							case INT_TYPE:
								node1.longValue = node2.intValue;
								break;
							case LONG_TYPE:
								node1.longValue = node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.longValue = (long) node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.longValue = (long) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.longValue = node2.charValue;
								break;
						}
						break;
					case FLOAT_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.floatValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.floatValue = node2.shortValue;
								break;
							case INT_TYPE:
								node1.floatValue = node2.intValue;
								break;
							case LONG_TYPE:
								node1.floatValue = node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.floatValue = node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.floatValue = (float) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.floatValue = node2.charValue;
								break;
						}
						break;
					case DOUBLE_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.doubleValue = node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.doubleValue = node2.shortValue;
								break;
							case INT_TYPE:
								node1.doubleValue = node2.intValue;
								break;
							case LONG_TYPE:
								node1.doubleValue = node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.doubleValue = node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.doubleValue = node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.doubleValue = node2.charValue;
								break;
						}
						break;
					case CHAR_TYPE:
						switch (t2) {
							case BYTE_TYPE:
								node1.charValue = (char) node2.byteValue;
								break;
							case SHORT_TYPE:
								node1.charValue = (char) node2.shortValue;
								break;
							case INT_TYPE:
								node1.charValue = (char) node2.intValue;
								break;
							case LONG_TYPE:
								node1.charValue = (char) node2.longValue;
								break;
							case FLOAT_TYPE:
								node1.charValue = (char) node2.floatValue;
								break;
							case DOUBLE_TYPE:
								node1.charValue = (char) node2.doubleValue;
								break;
							case CHAR_TYPE:
								node1.charValue = node2.charValue;
								break;
						}
						break;
				}
			} else if (c1 == HiClass.STRING_CLASS && c2 == HiClass.STRING_CLASS) {
				node1.stringValue = node2.stringValue;
			}
		} else {
			ctx.nodeValueType.returnType = NodeValueType.NodeValueReturnType.runtimeValue;
		}
		return node1.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		assert v1.node instanceof NodeType;
		HiClass c1 = ((NodeType) v1.node).getTypeClass();
		v1.valueClass = c1;

		HiClass c2;
		if (v2.originalValueClass != null) {
			c2 = v2.originalValueClass;
		} else {
			c2 = v2.valueClass;
		}

		if (c1.isPrimitive()) {
			castPrimitive(ctx, v1, v2);
		} else if (c1 == HiClass.OBJECT_CLASS) {
			v1.object = v2.object;
		} else if (c1.isArray()) {
			if (!c2.isArray() || !canCastArray((HiClassArray) c1, (HiClassArray) c2)) {
				errorCast(ctx, c2, c1);
				return;
			}
			v1.object = v2.object;
		} else if (c2.isArray()) {
			errorCast(ctx, c2, c1);
			return;
		} else if (v2.object == null) {
			v1.object = null;
		} else {
			c2 = ((HiObject) v2.object).clazz;
			if (!c2.isInstanceof(c1)) {
				errorCast(ctx, c2, c1);
				return;
			}
			v1.object = v2.object;
		}
		v1.valueType = ValueType.VALUE;
	}

	public static boolean canCastArray(HiClassArray from, HiClassArray to) {
		if (from.dimension != to.dimension) {
			return false;
		}
		if (from.cellClass.isPrimitive()) {
			return from.cellClass == to.cellClass;
		}
		if (to.cellClass.isPrimitive()) {
			return false;
		}
		if (from.cellClass.isInterface || to.cellClass.isInterface) {
			return true;
		}
		return to.cellClass.isInstanceof(from.cellClass);
	}

	private void castPrimitive(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.valueClass; // primitive
		HiClass c2 = v2.valueClass; // primitive or box or Number
		if (c2 == HiClass.NUMBER_CLASS) {
			if (v2.object == null) {
				ctx.throwRuntimeException("null pointer");
				return;
			} else if (v2.originalValueClass != null) {
				c2 = v2.originalValueClass;
			}
		}
		if (c2.getAutoboxedPrimitiveClass() != null) {
			c2 = c2.getAutoboxedPrimitiveClass();
		}
		assert c2.isPrimitive();

		PrimitiveType type1 = c1.getPrimitiveType();
		PrimitiveType type2 = c2.getPrimitiveType();
		switch (type1) {
			case BOOLEAN_TYPE:
				v1.bool = v2.bool;
				break;
			case CHAR_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.character = (char) v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.character = (char) v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.character = v2.character;
						break;
					case INT_TYPE:
						v1.character = (char) v2.intNumber;
						break;
					case LONG_TYPE:
						v1.character = (char) v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.character = (char) v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.character = (char) v2.doubleNumber;
						break;
				}
			case BYTE_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.byteNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.byteNumber = (byte) v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.byteNumber = (byte) v2.character;
						break;
					case INT_TYPE:
						v1.byteNumber = (byte) v2.intNumber;
						break;
					case LONG_TYPE:
						v1.byteNumber = (byte) v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.byteNumber = (byte) v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.byteNumber = (byte) v2.doubleNumber;
						break;
				}
			case SHORT_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.shortNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.shortNumber = v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.shortNumber = (short) v2.character;
						break;
					case INT_TYPE:
						v1.shortNumber = (short) v2.intNumber;
						break;
					case LONG_TYPE:
						v1.shortNumber = (short) v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.shortNumber = (short) v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.shortNumber = (short) v2.doubleNumber;
						break;
				}
			case INT_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.intNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.intNumber = v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.intNumber = v2.character;
						break;
					case INT_TYPE:
						v1.intNumber = v2.intNumber;
						break;
					case LONG_TYPE:
						v1.intNumber = (int) v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.intNumber = (int) v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.intNumber = (int) v2.doubleNumber;
						break;
				}
			case LONG_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.longNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.longNumber = v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.longNumber = v2.character;
						break;
					case INT_TYPE:
						v1.longNumber = v2.intNumber;
						break;
					case LONG_TYPE:
						v1.longNumber = v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.longNumber = (long) v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.longNumber = (long) v2.doubleNumber;
						break;
				}
			case FLOAT_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.floatNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.floatNumber = v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.floatNumber = v2.character;
						break;
					case INT_TYPE:
						v1.floatNumber = v2.intNumber;
						break;
					case LONG_TYPE:
						v1.floatNumber = v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.floatNumber = v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.floatNumber = (float) v2.doubleNumber;
						break;
				}
			case DOUBLE_TYPE:
				switch (type2) {
					case BYTE_TYPE:
						v1.doubleNumber = v2.byteNumber;
						break;
					case SHORT_TYPE:
						v1.doubleNumber = v2.shortNumber;
						break;
					case CHAR_TYPE:
						v1.doubleNumber = v2.character;
						break;
					case INT_TYPE:
						v1.doubleNumber = v2.intNumber;
						break;
					case LONG_TYPE:
						v1.doubleNumber = v2.longNumber;
						break;
					case FLOAT_TYPE:
						v1.doubleNumber = v2.floatNumber;
						break;
					case DOUBLE_TYPE:
						v1.doubleNumber = v2.doubleNumber;
						break;
				}
		}
	}
}
