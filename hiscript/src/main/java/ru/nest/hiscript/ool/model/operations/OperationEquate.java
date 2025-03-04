package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeArray;
import ru.nest.hiscript.ool.model.nodes.NodeConstructor;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class OperationEquate extends BinaryOperation {
	private static final HiOperation instance = new OperationEquate();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquate() {
		super(EQUATE);
	}

	@Override
	public boolean isStatement() {
		return true;
	}

	@Override
	public HiClass getOperationResultClass(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType node1, NodeValueType node2) {
		if (!node1.isVariable()) {
			validationInfo.error("variable expected", node1.token);
		} else {
			if (node2.clazz.isNull()) {
				if (!node1.clazz.isPrimitive()) {
					return node1.clazz;
				}
			} else {
				HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
				HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
				if (c1.isPrimitive() && c2.isPrimitive()) {
					PrimitiveType t1 = c1.getPrimitiveType();
					PrimitiveType t2 = c2.getPrimitiveType();
					if (t1 == BOOLEAN_TYPE || t2 == BOOLEAN_TYPE) {
						if (t1 == t2) {
							if (node2.isCompileValue()) {
								node1.booleanValue = node2.booleanValue;
							}
							return HiClassPrimitive.BOOLEAN;
						}
					} else if (node2.isCompileValue()) {
						switch (t1) {
							case CHAR_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										node1.charValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.CHAR;
									case BYTE_TYPE:
										if (node2.byteValue >= Character.MIN_VALUE) {
											node1.charValue = (char) node2.byteValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
									case SHORT_TYPE:
										if (node2.shortValue >= Character.MIN_VALUE && node2.shortValue <= Character.MAX_VALUE) {
											node1.charValue = (char) node2.shortValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
									case INT_TYPE:
										if (node2.intValue >= Character.MIN_VALUE && node2.intValue <= Character.MAX_VALUE) {
											node1.charValue = (char) node2.intValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
								}
								break;

							case BYTE_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										if (node2.charValue >= Byte.MIN_VALUE && node2.charValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.charValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
									case BYTE_TYPE:
										node1.byteValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.BYTE;
									case SHORT_TYPE:
										if (node2.shortValue >= Byte.MIN_VALUE && node2.shortValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.shortValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
									case INT_TYPE:
										if (node2.intValue >= Byte.MIN_VALUE && node2.intValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.intValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
								}
								break;

							case SHORT_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										if (node2.charValue >= Short.MIN_VALUE && node2.charValue <= Short.MAX_VALUE) {
											node1.shortValue = (short) node2.charValue;
											return node1.valueClass = HiClassPrimitive.SHORT;
										}
										break;
									case BYTE_TYPE:
										node1.shortValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.SHORT;
									case SHORT_TYPE:
										node1.shortValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.SHORT;
									case INT_TYPE:
										if (node2.intValue >= Short.MIN_VALUE && node2.intValue <= Short.MAX_VALUE) {
											node1.shortValue = (short) node2.intValue;
											return node1.valueClass = HiClassPrimitive.SHORT;
										}
										break;
								}
								break;

							case INT_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										node1.intValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case BYTE_TYPE:
										node1.intValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case SHORT_TYPE:
										node1.intValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case INT_TYPE:
										node1.intValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.INT;
								}
								break;

							case LONG_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										node1.longValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case BYTE_TYPE:
										node1.longValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case SHORT_TYPE:
										node1.longValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case INT_TYPE:
										node1.longValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case LONG_TYPE:
										node1.longValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.LONG;
								}
								break;

							case FLOAT_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										node1.floatValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case BYTE_TYPE:
										node1.floatValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case SHORT_TYPE:
										node1.floatValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case INT_TYPE:
										node1.floatValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case LONG_TYPE:
										node1.floatValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case FLOAT_TYPE:
										node1.floatValue = node2.floatValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
								}
								break;

							case DOUBLE_TYPE:
								switch (t2) {
									case CHAR_TYPE:
										node1.doubleValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case BYTE_TYPE:
										node1.doubleValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case SHORT_TYPE:
										node1.doubleValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case INT_TYPE:
										node1.doubleValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case LONG_TYPE:
										node1.doubleValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case FLOAT_TYPE:
										node1.doubleValue = node2.floatValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case DOUBLE_TYPE:
										node1.doubleValue = node2.doubleValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
								}
								break;
						}
					} else {
						switch (t1) {
							case CHAR_TYPE:
								if (t2 == CHAR_TYPE) {
									return HiClassPrimitive.CHAR;
								}
								break;
							case BYTE_TYPE:
								if (t2 == BYTE_TYPE) {
									return HiClassPrimitive.BYTE;
								}
								break;
							case SHORT_TYPE:
								if (t2 == BYTE_TYPE || t2 == SHORT_TYPE) {
									return HiClassPrimitive.SHORT;
								}
								break;
							case INT_TYPE:
								switch (t2) {
									case CHAR_TYPE:
									case BYTE_TYPE:
									case SHORT_TYPE:
									case INT_TYPE:
										return HiClassPrimitive.INT;
								}
								break;
							case LONG_TYPE:
								switch (t2) {
									case CHAR_TYPE:
									case BYTE_TYPE:
									case SHORT_TYPE:
									case INT_TYPE:
									case LONG_TYPE:
										return HiClassPrimitive.LONG;
								}
								break;
							case FLOAT_TYPE:
								switch (t2) {
									case CHAR_TYPE:
									case BYTE_TYPE:
									case SHORT_TYPE:
									case INT_TYPE:
									case LONG_TYPE:
									case FLOAT_TYPE:
										return HiClassPrimitive.FLOAT;
								}
								break;
							case DOUBLE_TYPE:
								return HiClassPrimitive.DOUBLE;
						}
					}
				} else if (c1 == HiClass.NUMBER_CLASS && c2.isNumber()) {
					return c2.boxed();
				} else if (node2.clazz.isInstanceof(node1.clazz)) {
					// @generics
					if (node2.node instanceof NodeConstructor) {
						((NodeConstructor) node2.node).validateDeclarationGenericType(node1.type, validationInfo, ctx);
					} else if (node2.node instanceof NodeArray) {
						((NodeArray) node2.node).validateDeclarationGenericType(node1.type, validationInfo, ctx);
					}
					return c1;
				}
			}
		}
		errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
		return null;
	}

	@Override
	public void getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeValueType... nodes) {
		NodeValueType node1 = nodes[0];
		HiNodeIF node = node1.node != null ? node1.node : node1.resolvedValueVariable;
		checkFinal(validationInfo, ctx, node, true);
		super.getOperationResultType(validationInfo, ctx, nodes);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType == ValueType.VARIABLE) {
			// 1. copy variable from v1
			HiField<?> variable = v1.variable;

			// 2. copy v2 to v1
			v2.copyTo(v1);

			// 3. set v1 variable again
			v1.valueType = ValueType.VARIABLE;
			v1.variable = variable;

			// 4. set value of variable from v2
			variable.set(ctx, v2);
			variable.initialized = true;
		} else if (v1.valueType == ValueType.ARRAY_INDEX) {
			HiArrays.setArrayIndex(v1.valueClass, v1.parentArray, v1.arrayIndex, v2, v1);
		}
	}
}
