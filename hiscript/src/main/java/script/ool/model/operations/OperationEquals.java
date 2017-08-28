package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;

public class OperationEquals extends BinaryOperation {
	private static Operation instance = new OperationEquals();

	public static Operation getInstance() {
		return instance;
	}

	private OperationEquals() {
		super("==", EQUALS);
	}

	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;
		v1.type = TYPE_BOOLEAN;

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (isP1 && isP2) {
			int t1 = FieldPrimitive.getType(c1);
			int t2 = FieldPrimitive.getType(c2);
			switch (t1) {
				case BOOLEAN:
					if (t2 == BOOLEAN) {
						v1.bool = v1.bool == v2.bool;
						return;
					}
					break;

				case CHAR:
					switch (t2) {
						case CHAR:
							v1.bool = v1.character == v2.character;
							return;

						case BYTE:
							v1.bool = v1.character == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.character == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.character == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.character == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.character == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.character == v2.doubleNumber;
							return;
					}
					break;

				case BYTE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.byteNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.byteNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.byteNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.byteNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.byteNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.byteNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.byteNumber == v2.doubleNumber;
							return;
					}
					break;

				case SHORT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.shortNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.shortNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.shortNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.shortNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.shortNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.shortNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.shortNumber == v2.doubleNumber;
							return;
					}
					break;

				case INT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.intNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.intNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.intNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.intNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.intNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.intNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.intNumber == v2.doubleNumber;
							return;
					}
					break;

				case LONG:
					switch (t2) {
						case CHAR:
							v1.bool = v1.longNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.longNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.longNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.longNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.longNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.longNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.longNumber == v2.doubleNumber;
							return;
					}
					break;

				case FLOAT:
					switch (t2) {
						case CHAR:
							v1.bool = v1.floatNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.floatNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.floatNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.floatNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.floatNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.floatNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.floatNumber == v2.doubleNumber;
							return;
					}
					break;

				case DOUBLE:
					switch (t2) {
						case CHAR:
							v1.bool = v1.doubleNumber == v2.character;
							return;

						case BYTE:
							v1.bool = v1.doubleNumber == v2.byteNumber;
							return;

						case SHORT:
							v1.bool = v1.doubleNumber == v2.shortNumber;
							return;

						case INT:
							v1.bool = v1.doubleNumber == v2.intNumber;
							return;

						case LONG:
							v1.bool = v1.doubleNumber == v2.longNumber;
							return;

						case FLOAT:
							v1.bool = v1.doubleNumber == v2.floatNumber;
							return;

						case DOUBLE:
							v1.bool = v1.doubleNumber == v2.doubleNumber;
							return;
					}
					break;
			}
		} else if (!isP1 && !isP2) {
			Object o1;
			if (c1.isArray()) {
				o1 = v1.array;
			} else if (c1.isNull()) {
				o1 = null;
			} else {
				o1 = v1.object;
			}

			Object o2;
			if (c2.isArray()) {
				o2 = v2.array;
			} else if (c2.isNull()) {
				o2 = null;
			} else {
				o2 = v2.object;
			}

			v1.bool = o1 == o2;
			return;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
