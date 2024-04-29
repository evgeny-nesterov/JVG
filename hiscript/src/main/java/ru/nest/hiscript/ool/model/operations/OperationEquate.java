package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
					int t1 = c1.getPrimitiveType();
					int t2 = c2.getPrimitiveType();
					if (t1 == VAR) {
						t1 = t2;
					}
					if (t1 == BOOLEAN || t2 == BOOLEAN) {
						if (t1 == t2) {
							if (node2.isCompileValue()) {
								node1.booleanValue = node2.booleanValue;
							}
							return HiClassPrimitive.BOOLEAN;
						}
					} else if (node2.isCompileValue()) {
						switch (t1) {
							case CHAR:
								switch (t2) {
									case CHAR:
										node1.charValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.CHAR;
									case BYTE:
										if (node2.byteValue >= Character.MIN_VALUE) {
											node1.charValue = (char) node2.byteValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
									case SHORT:
										if (node2.shortValue >= Character.MIN_VALUE && node2.shortValue <= Character.MAX_VALUE) {
											node1.charValue = (char) node2.shortValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
									case INT:
										if (node2.intValue >= Character.MIN_VALUE && node2.intValue <= Character.MAX_VALUE) {
											node1.charValue = (char) node2.intValue;
											return node1.valueClass = HiClassPrimitive.CHAR;
										}
										break;
								}
								break;

							case BYTE:
								switch (t2) {
									case CHAR:
										if (node2.charValue >= Byte.MIN_VALUE && node2.charValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.charValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
									case BYTE:
										node1.byteValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.BYTE;
									case SHORT:
										if (node2.shortValue >= Byte.MIN_VALUE && node2.shortValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.shortValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
									case INT:
										if (node2.intValue >= Byte.MIN_VALUE && node2.intValue <= Byte.MAX_VALUE) {
											node1.byteValue = (byte) node2.intValue;
											return node1.valueClass = HiClassPrimitive.BYTE;
										}
										break;
								}
								break;

							case SHORT:
								switch (t2) {
									case CHAR:
										if (node2.charValue >= Short.MIN_VALUE && node2.charValue <= Short.MAX_VALUE) {
											node1.shortValue = (short) node2.charValue;
											return node1.valueClass = HiClassPrimitive.SHORT;
										}
										break;
									case BYTE:
										node1.shortValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.SHORT;
									case SHORT:
										node1.shortValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.SHORT;
									case INT:
										if (node2.intValue >= Short.MIN_VALUE && node2.intValue <= Short.MAX_VALUE) {
											node1.shortValue = (short) node2.intValue;
											return node1.valueClass = HiClassPrimitive.SHORT;
										}
										break;
								}
								break;

							case INT:
								switch (t2) {
									case CHAR:
										node1.intValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case BYTE:
										node1.intValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case SHORT:
										node1.intValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.INT;
									case INT:
										node1.intValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.INT;
								}
								break;

							case LONG:
								switch (t2) {
									case CHAR:
										node1.longValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case BYTE:
										node1.longValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case SHORT:
										node1.longValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case INT:
										node1.longValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.LONG;
									case LONG:
										node1.longValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.LONG;
								}
								break;

							case FLOAT:
								switch (t2) {
									case CHAR:
										node1.floatValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case BYTE:
										node1.floatValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case SHORT:
										node1.floatValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case INT:
										node1.floatValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case LONG:
										node1.floatValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
									case FLOAT:
										node1.floatValue = node2.floatValue;
										return node1.valueClass = HiClassPrimitive.FLOAT;
								}
								break;

							case DOUBLE:
								switch (t2) {
									case CHAR:
										node1.doubleValue = node2.charValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case BYTE:
										node1.doubleValue = node2.byteValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case SHORT:
										node1.doubleValue = node2.shortValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case INT:
										node1.doubleValue = node2.intValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case LONG:
										node1.doubleValue = node2.longValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case FLOAT:
										node1.doubleValue = node2.floatValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
									case DOUBLE:
										node1.doubleValue = node2.doubleValue;
										return node1.valueClass = HiClassPrimitive.DOUBLE;
								}
								break;
						}
					} else {
						switch (t1) {
							case CHAR:
								if (t2 == CHAR) {
									return HiClassPrimitive.CHAR;
								}
								break;

							case BYTE:
								if (t2 == BYTE) {
									return HiClassPrimitive.BYTE;
								}
								break;

							case SHORT:
								if (t2 == BYTE || t2 == SHORT) {
									return HiClassPrimitive.SHORT;
								}
								break;

							case INT:
								switch (t2) {
									case CHAR:
									case BYTE:
									case SHORT:
									case INT:
										return HiClassPrimitive.INT;
								}
								break;

							case LONG:
								switch (t2) {
									case CHAR:
									case BYTE:
									case SHORT:
									case INT:
									case LONG:
										return HiClassPrimitive.LONG;
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
								}
								break;

							case DOUBLE:
								return HiClassPrimitive.DOUBLE;
						}
					}
				} else if (c2.isNull() || c2.isInstanceof(c1)) {
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
		if (v2.valueType == Value.VARIABLE && !v2.variable.isInitialized(ctx)) {
			ctx.throwRuntimeException("variable not initialized: " + v2.variable.name);
			return;
		}

		if (v1.valueType == Value.VARIABLE) {
			// 1. copy variable from v1
			HiField<?> variable = v1.variable;
			if (variable.initialized && variable.getModifiers().isFinal()) {
				ctx.throwRuntimeException("cannot assign a value to final variable '" + variable.name + "'");
				return;
			}

			// 2. copy v2 to v1
			v2.copyTo(v1);

			// 3. set v1 variable again
			v1.valueType = Value.VARIABLE;
			v1.variable = variable;

			// 4. set value of variable from v2
			variable.set(ctx, v2);
			variable.initialized = true;

			// DEBUG
			// System.out.println(v1.variable.name + " (" + v1.variable.getClazz(ctx)+ ") = " + v1.variable.get() + ", " + v1.get() + ", " + v1.type);
		} else if (v1.valueType == Value.ARRAY_INDEX) {
			if (!HiClass.autoCast(ctx, v2.valueClass, v1.valueClass, v2.valueType == Value.VALUE, true)) {
				ctx.throwRuntimeException("incompatible types; found " + v2.valueClass + ", required " + v1.valueClass);
				return;
			}
			HiArrays.setArrayIndex(v1.valueClass, v1.parentArray, v1.arrayIndex, v2, v1);
		} else {
			errorUnexpectedType(ctx);
		}
	}
}
