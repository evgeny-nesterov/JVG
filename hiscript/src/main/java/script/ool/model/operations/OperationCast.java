package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.PrimitiveTypes;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.classes.ClazzArray;
import script.ool.model.fields.FieldPrimitive;

public class OperationCast extends BinaryOperation implements PrimitiveTypes {
	private static Operation instance = new OperationCast();

	public static Operation getInstance() {
		return instance;
	}

	private OperationCast() {
		super("(cast)", CAST);
	}

	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v1.valueType != Value.TYPE) {
			ctx.throwException("type is expected");
			return;
		}

		Clazz t1 = v1.type = v1.variableType.getClazz(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		if (t1.isPrimitive()) {
			// cast primitive
			castPrimitive(ctx, v1, v2);
		} else if (t1.isArray()) {
			// cast array
			ClazzArray at1 = (ClazzArray) t1;
			Clazz t2 = v2.type;
			if (!canCastArray(at1, t2)) {
				errorCast(ctx, v2.type, v1.type);
				return;
			}

			v1.array = v2.array;
		} else {
			// cast object
			Clazz t2 = v2.object.clazz;

			if (!t2.isInstanceof(t1)) {
				errorCast(ctx, t2, t1);
				return;
			}

			v1.object = v2.object;
		}

		v1.valueType = Value.VALUE;
	}

	public static boolean canCastArray(ClazzArray from, Clazz to) {
		if (!to.isArray()) {
			return false;
		}

		ClazzArray at1 = from;
		ClazzArray at2 = (ClazzArray) to;
		if (at1.dimension != at2.dimension) {
			return false;
		}

		if (!at2.cellClass.isInstanceof(at1.cellClass)) {
			return false;
		}
		return true;
	}

	private void castPrimitive(RuntimeContext ctx, Value v1, Value v2) {
		if (!v2.type.isPrimitive()) {
			errorCast(ctx, v2.type, v1.type);
			return;
		}

		int type1 = FieldPrimitive.getType(v1.type);
		int type2 = FieldPrimitive.getType(v2.type);
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
