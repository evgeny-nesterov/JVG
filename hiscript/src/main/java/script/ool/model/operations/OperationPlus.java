package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;
import script.ool.model.nodes.NodeString;

public class OperationPlus extends BinaryOperation {
	private static Operation instance = new OperationPlus();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPlus() {
		super("+", PLUS);
	}

	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;
		if (v1.object != null && c1.isObject()) {
			c1 = v1.object.clazz;
		}
		if (v2.object != null && c2.isObject()) {
			c2 = v2.object.clazz;
		}

		boolean isS1 = c1 != null && "String".equals(c1.fullName);
		boolean isS2 = c2 != null && "String".equals(c2.fullName);
		if (isS1 || isS2) {
			char[] chars1 = v1.getString(ctx);
			char[] chars2 = v2.getString(ctx);
			char[] chars = new char[chars1.length + chars2.length];
			System.arraycopy(chars1, 0, chars, 0, chars1.length);
			System.arraycopy(chars2, 0, chars, chars1.length, chars2.length);
			NodeString.createString(ctx, chars);

			v1.type = ctx.value.type;
			v1.object = ctx.value.object;
			return;
		}

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (!isP1 || !isP2) {
			errorInvalidOperator(ctx, c1, c2);
			return;
		}

		int t1 = FieldPrimitive.getType(c1);
		int t2 = FieldPrimitive.getType(c2);
		switch (t1) {
			case CHAR:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.character + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.character + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.character + v2.doubleNumber;
						return;
				}
				break;

			case BYTE:
				switch (t2) {
					case CHAR:
						v1.intNumber = v1.byteNumber + v2.character;
						v1.type = TYPE_INT;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.byteNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.byteNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.byteNumber + v2.doubleNumber;
						return;
				}
				break;

			case SHORT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.shortNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.shortNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.shortNumber + v2.doubleNumber;
						return;
				}
				break;

			case INT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.intNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.intNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.intNumber + v2.doubleNumber;
						return;
				}
				break;

			case LONG:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.longNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.longNumber + v2.doubleNumber;
						return;
				}
				break;

			case FLOAT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_FLOAT;
						v1.floatNumber = v1.floatNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.floatNumber + v2.doubleNumber;
						return;
				}
				break;

			case DOUBLE:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.character;
						return;

					case BYTE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.longNumber;
						return;

					case FLOAT:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.floatNumber;
						return;

					case DOUBLE:
						v1.type = TYPE_DOUBLE;
						v1.doubleNumber = v1.doubleNumber + v2.doubleNumber;
						return;
				}
				break;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
