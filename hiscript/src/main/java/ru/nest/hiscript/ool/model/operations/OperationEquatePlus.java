package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiArrays;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationEquatePlus extends BinaryOperation {
	private static HiOperation instance = new OperationEquatePlus();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationEquatePlus() {
		super("+=", EQUATE_PLUS);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		// TODO check
		return node1.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;

		if (v1.valueType != Value.VARIABLE && v1.valueType != Value.ARRAY_INDEX) {
			errorUnexpectedType(ctx);
			return;
		}

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (isP1 && isP2) {
			int t1 = HiFieldPrimitive.getType(c1);
			int t2 = HiFieldPrimitive.getType(c2);
			if (t1 != BOOLEAN && t2 != BOOLEAN) {
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

				if (v1.valueType == Value.VARIABLE) {
					v1.variable.set(ctx, v1);
				} else if (v1.valueType == Value.ARRAY_INDEX) {
					v1.copyToArray(v1);
				}
				return;
			}
		} else if (c1.fullName.equals(HiClass.STRING_CLASS_NAME)) {
			char[] chars1 = v1.getString(ctx);
			char[] chars2 = v2.getString(ctx);
			char[] chars = new char[chars1.length + chars2.length];
			System.arraycopy(chars1, 0, chars, 0, chars1.length);
			System.arraycopy(chars2, 0, chars, chars1.length, chars2.length);
			NodeString.createString(ctx, chars);

			if (v1.valueType == Value.VARIABLE) {
				v1.variable.set(ctx, ctx.value);
				return;
			} else if (v1.valueType == Value.ARRAY_INDEX) {
				HiArrays.setArrayIndex(v1.type, v1.parentArray, v1.arrayIndex, ctx.value, v1);
				return;
			}
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
