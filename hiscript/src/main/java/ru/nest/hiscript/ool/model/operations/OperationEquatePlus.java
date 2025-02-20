package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquatePlus extends BinaryOperation {
	private static final HiOperation instance = new OperationEquatePlus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquatePlus() {
		super(EQUATE_PLUS);
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

		if (!node1.clazz.isStringClass()) {
			HiClass c1 = node1.clazz.getAutoboxedPrimitiveClass() == null ? node1.clazz : node1.clazz.getAutoboxedPrimitiveClass();
			HiClass c2 = node2.clazz.getAutoboxedPrimitiveClass() == null ? node2.clazz : node2.clazz.getAutoboxedPrimitiveClass();
			if (!c1.isPrimitive() || !c2.isPrimitive()) {
				errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
				return null;
			}
			if (c1 == HiClassPrimitive.BOOLEAN || c2 == HiClassPrimitive.BOOLEAN) {
				errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
				return null;
			}
			if (node1.clazz == HiClassPrimitive.BYTE.getAutoboxClass() || node1.clazz == HiClassPrimitive.SHORT.getAutoboxClass() || node1.clazz == HiClassPrimitive.CHAR.getAutoboxClass()) {
				errorInvalidOperator(validationInfo, node1.token, node1.clazz, node2.clazz);
				return null;
			}
		}

		HiNodeIF node = node1.node != null ? node1.node : node1.resolvedValueVariable;
		checkFinal(validationInfo, ctx, node, true);
		return node1.clazz;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.getOperationClass();
		HiClass c2 = v2.getOperationClass();
		if (c1.isPrimitive() && c2.isPrimitive()) {
			int t1 = c1.getPrimitiveType();
			int t2 = c2.getPrimitiveType();
			switch (t1) {
				case CHAR:
					switch (t2) {
						case CHAR:
							v1.character += v2.character;
							break;
						case BYTE:
							v1.character += v2.byteNumber;
							break;
						case SHORT:
							v1.character += v2.shortNumber;
							break;
						case INT:
							v1.character += v2.intNumber;
							break;
						case LONG:
							v1.character += v2.longNumber;
							break;
						case FLOAT:
							v1.character += v2.floatNumber;
							break;
						case DOUBLE:
							v1.character += v2.doubleNumber;
							break;
					}
					break;
				case BYTE:
					switch (t2) {
						case CHAR:
							v1.byteNumber += v2.character;
							break;
						case BYTE:
							v1.byteNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.byteNumber += v2.shortNumber;
							break;
						case INT:
							v1.byteNumber += v2.intNumber;
							break;
						case LONG:
							v1.byteNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.byteNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.byteNumber += v2.doubleNumber;
							break;
					}
					break;
				case SHORT:
					switch (t2) {
						case CHAR:
							v1.shortNumber += v2.character;
							break;
						case BYTE:
							v1.shortNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.shortNumber += v2.shortNumber;
							break;
						case INT:
							v1.shortNumber += v2.intNumber;
							break;
						case LONG:
							v1.shortNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.shortNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.shortNumber += v2.doubleNumber;
							break;
					}
					break;
				case INT:
					switch (t2) {
						case CHAR:
							v1.intNumber += v2.character;
							break;
						case BYTE:
							v1.intNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.intNumber += v2.shortNumber;
							break;
						case INT:
							v1.intNumber += v2.intNumber;
							break;
						case LONG:
							v1.intNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.intNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.intNumber += v2.doubleNumber;
							break;
					}
					break;
				case LONG:
					switch (t2) {
						case CHAR:
							v1.longNumber += v2.character;
							break;
						case BYTE:
							v1.longNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.longNumber += v2.shortNumber;
							break;
						case INT:
							v1.longNumber += v2.intNumber;
							break;
						case LONG:
							v1.longNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.longNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.longNumber += v2.doubleNumber;
							break;
					}
					break;
				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.floatNumber += v2.character;
							break;
						case BYTE:
							v1.floatNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.floatNumber += v2.shortNumber;
							break;
						case INT:
							v1.floatNumber += v2.intNumber;
							break;
						case LONG:
							v1.floatNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.floatNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.floatNumber += v2.doubleNumber;
							break;
					}
					break;
				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.doubleNumber += v2.character;
							break;
						case BYTE:
							v1.doubleNumber += v2.byteNumber;
							break;
						case SHORT:
							v1.doubleNumber += v2.shortNumber;
							break;
						case INT:
							v1.doubleNumber += v2.intNumber;
							break;
						case LONG:
							v1.doubleNumber += v2.longNumber;
							break;
						case FLOAT:
							v1.doubleNumber += v2.floatNumber;
							break;
						case DOUBLE:
							v1.doubleNumber += v2.doubleNumber;
							break;
					}
					break;
			}

			// @autoboxing
			if (v1.valueClass.isObject()) {
				v1.valueClass = c1;
				v1.object = ((HiClassPrimitive) c1).box(ctx, v1);
			}

			if (v1.valueType == Value.VARIABLE) {
				v1.variable.set(ctx, v1);
			} else if (v1.valueType == Value.ARRAY_INDEX) {
				v1.copyToArray(v1);
			}
		} else if (c1.isStringClass()) {
			char[] chars1 = v1.getString(ctx);
			char[] chars2 = v2.getString(ctx);
			char[] chars = new char[chars1.length + chars2.length];
			System.arraycopy(chars1, 0, chars, 0, chars1.length);
			System.arraycopy(chars2, 0, chars, chars1.length, chars2.length);
			NodeString.createString(ctx, chars);

			if (v1.valueType == Value.VARIABLE) {
				v1.variable.set(ctx, ctx.value);
			} else if (v1.valueType == Value.ARRAY_INDEX) {
				HiArrays.setArrayIndex(v1.valueClass, v1.parentArray, v1.arrayIndex, ctx.value, v1);
			}
		}
	}
}
