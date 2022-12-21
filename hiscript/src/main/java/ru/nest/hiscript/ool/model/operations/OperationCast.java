package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiOperation;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class OperationCast extends BinaryOperation implements PrimitiveTypes {
	private static HiOperation instance = new OperationCast();

	public static HiOperation getInstance() {
		return instance;
	}

	private OperationCast() {
		super("(cast)", CAST);
	}

	@Override
	public HiClass getOperationResultType(ValidationInfo validationInfo, CompileClassContext ctx, NodeExpressionNoLS.NodeOperandType node1, NodeExpressionNoLS.NodeOperandType node2) {
		HiClass c1 = node1.type;
		HiClass c2 = node2.type;
		if (c1.isPrimitive()) {
			int t1 = HiFieldPrimitive.getType(c1);
			int t2 = HiFieldPrimitive.getType(c2);
			if (t2 == BOOLEAN && t1 != BOOLEAN) {
				errorCast(validationInfo, node1.node.getToken(), c2, c1);
			}
		} else if (c1.isArray() && c2.isArray()) {
			// c1 and c2 has to be in one hierarchy path
			if (!canCastArray((HiClassArray) c1, c2) && !canCastArray((HiClassArray) c2, c1)) {
				errorCast(validationInfo, node1.node.getToken(), c2, c1);
			}
		} else {
			// c1 and c2 has to be in one hierarchy path
			if (!c2.isInstanceof(c1) && !c1.isInstanceof(c2)) {
				errorCast(validationInfo, node1.node.getToken(), c2, c1);
			}
		}
		return node1.type;
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType != Value.TYPE) {
			ctx.throwRuntimeException("type is expected");
			return;
		}

		HiClass c1 = v1.type = v1.variableType.getClass(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		if (c1.isPrimitive()) {
			castPrimitive(ctx, v1, v2);
		} else if (c1.isArray()) {
			if (!canCastArray((HiClassArray) c1, v2.type)) {
				errorCast(ctx, v2.type, v1.type);
				return;
			}
			v1.array = v2.array;
		} else {
			HiClass c2 = v2.object.clazz;
			if (!c2.isInstanceof(c1)) {
				errorCast(ctx, c2, c1);
				return;
			}
			v1.object = v2.object;
		}
		v1.valueType = Value.VALUE;
	}

	public static boolean canCastArray(HiClassArray from, HiClass to) {
		if (!to.isArray()) {
			return false;
		}

		HiClassArray at1 = from;
		HiClassArray at2 = (HiClassArray) to;
		if (at1.dimension != at2.dimension) {
			return false;
		}
		if (at1.cellClass.isPrimitive()) {
			return at1.cellClass == at2.cellClass;
		}
		if (at2.cellClass.isPrimitive()) {
			return false;
		}
		if (at1.cellClass.isInterface || at2.cellClass.isInterface) {
			return true;
		}
		return at2.cellClass.isInstanceof(at1.cellClass);
	}

	private void castPrimitive(RuntimeContext ctx, Value v1, Value v2) {
		if (!v2.type.isPrimitive()) {
			errorCast(ctx, v2.type, v1.type);
			return;
		}

		int type1 = HiFieldPrimitive.getType(v1.type);
		int type2 = HiFieldPrimitive.getType(v2.type);
		switch (type1) {
			case BOOLEAN:
				castBoolean(ctx, v1, v2, type2);
				break;

			case CHAR:
				castCharacter(ctx, v1, v2, type2);
				break;

			case BYTE:
				castByte(ctx, v1, v2, type2);
				break;

			case SHORT:
				castShort(ctx, v1, v2, type2);
				break;

			case INT:
				castInt(ctx, v1, v2, type2);
				break;

			case LONG:
				castLong(ctx, v1, v2, type2);
				break;

			case FLOAT:
				castFloat(ctx, v1, v2, type2);
				break;

			case DOUBLE:
				castDouble(ctx, v1, v2, type2);
				break;
		}
	}

	private void castBoolean(RuntimeContext ctx, Value v1, Value v2, int type) {
		if (type != BOOLEAN) {
			errorCast(ctx, v2.type, v1.type);
			return;
		}
		v1.bool = v2.bool;
	}

	private void castCharacter(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.character = (char) v2.byteNumber;
				break;

			case SHORT:
				v1.character = (char) v2.shortNumber;
				break;

			case CHAR:
				v1.character = v2.character;
				break;

			case INT:
				v1.character = (char) v2.intNumber;
				break;

			case LONG:
				v1.character = (char) v2.longNumber;
				break;

			case FLOAT:
				v1.character = (char) v2.floatNumber;
				break;

			case DOUBLE:
				v1.character = (char) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castByte(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.byteNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.byteNumber = (byte) v2.shortNumber;
				break;

			case CHAR:
				v1.byteNumber = (byte) v2.character;
				break;

			case INT:
				v1.byteNumber = (byte) v2.intNumber;
				break;

			case LONG:
				v1.byteNumber = (byte) v2.longNumber;
				break;

			case FLOAT:
				v1.byteNumber = (byte) v2.floatNumber;
				break;

			case DOUBLE:
				v1.byteNumber = (byte) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castShort(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.shortNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.shortNumber = v2.shortNumber;
				break;

			case CHAR:
				v1.shortNumber = (short) v2.character;
				break;

			case INT:
				v1.shortNumber = (short) v2.intNumber;
				break;

			case LONG:
				v1.shortNumber = (short) v2.longNumber;
				break;

			case FLOAT:
				v1.shortNumber = (short) v2.floatNumber;
				break;

			case DOUBLE:
				v1.shortNumber = (short) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castInt(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.intNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.intNumber = v2.shortNumber;
				break;

			case CHAR:
				v1.intNumber = v2.character;
				break;

			case INT:
				v1.intNumber = v2.intNumber;
				break;

			case LONG:
				v1.intNumber = (int) v2.longNumber;
				break;

			case FLOAT:
				v1.intNumber = (int) v2.floatNumber;
				break;

			case DOUBLE:
				v1.intNumber = (int) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castFloat(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.floatNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.floatNumber = v2.shortNumber;
				break;

			case CHAR:
				v1.floatNumber = v2.character;
				break;

			case INT:
				v1.floatNumber = v2.intNumber;
				break;

			case LONG:
				v1.floatNumber = v2.longNumber;
				break;

			case FLOAT:
				v1.floatNumber = v2.floatNumber;
				break;

			case DOUBLE:
				v1.floatNumber = (float) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castLong(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.longNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.longNumber = v2.shortNumber;
				break;

			case CHAR:
				v1.longNumber = v2.character;
				break;

			case INT:
				v1.longNumber = v2.intNumber;
				break;

			case LONG:
				v1.longNumber = v2.longNumber;
				break;

			case FLOAT:
				v1.longNumber = (long) v2.floatNumber;
				break;

			case DOUBLE:
				v1.longNumber = (long) v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}

	private void castDouble(RuntimeContext ctx, Value v1, Value v2, int type) {
		switch (type) {
			case BYTE:
				v1.doubleNumber = v2.byteNumber;
				break;

			case SHORT:
				v1.doubleNumber = v2.shortNumber;
				break;

			case CHAR:
				v1.doubleNumber = v2.character;
				break;

			case INT:
				v1.doubleNumber = v2.intNumber;
				break;

			case LONG:
				v1.doubleNumber = v2.longNumber;
				break;

			case FLOAT:
				v1.doubleNumber = v2.floatNumber;
				break;

			case DOUBLE:
				v1.doubleNumber = v2.doubleNumber;
				break;

			default:
				errorCast(ctx, v2.type, v1.type);
		}
	}
}
