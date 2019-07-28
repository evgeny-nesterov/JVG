package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;

public class OperationEquateMultiply extends BinaryOperation {
	private static Operation instance = new OperationEquateMultiply();

	public static Operation getInstance() {
		return instance;
	}

	private OperationEquateMultiply() {
		super("*=", EQUATE_MULTIPLY);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;

		if (v1.valueType != Value.VARIABLE && v1.valueType != Value.ARRAY_INDEX) {
			errorUnexpectedType(ctx);
			return;
		}

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (isP1 && isP2) {
			int t1 = FieldPrimitive.getType(c1);
			int t2 = FieldPrimitive.getType(c2);
			if (t1 != BOOLEAN && t2 != BOOLEAN) {
				switch (t1) {
					case CHAR:
						switch (t2) {
							case CHAR:
								v1.character *= v2.character;
								break;

							case BYTE:
								v1.character *= v2.byteNumber;
								break;

							case SHORT:
								v1.character *= v2.shortNumber;
								break;

							case INT:
								v1.character *= v2.intNumber;
								break;

							case LONG:
								v1.character *= v2.longNumber;
								break;

							case FLOAT:
								v1.character *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.character *= v2.doubleNumber;
								break;
						}
						break;

					case BYTE:
						switch (t2) {
							case CHAR:
								v1.byteNumber *= v2.character;
								break;

							case BYTE:
								v1.byteNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.byteNumber *= v2.shortNumber;
								break;

							case INT:
								v1.byteNumber *= v2.intNumber;
								break;

							case LONG:
								v1.byteNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.byteNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.byteNumber *= v2.doubleNumber;
								break;
						}
						break;

					case SHORT:
						switch (t2) {
							case CHAR:
								v1.shortNumber *= v2.character;
								break;

							case BYTE:
								v1.shortNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.shortNumber *= v2.shortNumber;
								break;

							case INT:
								v1.shortNumber *= v2.intNumber;
								break;

							case LONG:
								v1.shortNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.shortNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.shortNumber *= v2.doubleNumber;
								break;
						}
						break;

					case INT:
						switch (t2) {
							case CHAR:
								v1.intNumber *= v2.character;
								break;

							case BYTE:
								v1.intNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.intNumber *= v2.shortNumber;
								break;

							case INT:
								v1.intNumber *= v2.intNumber;
								break;

							case LONG:
								v1.intNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.intNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.intNumber *= v2.doubleNumber;
								break;
						}
						break;

					case LONG:
						switch (t2) {
							case CHAR:
								v1.longNumber *= v2.character;
								break;

							case BYTE:
								v1.longNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.longNumber *= v2.shortNumber;
								break;

							case INT:
								v1.longNumber *= v2.intNumber;
								break;

							case LONG:
								v1.longNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.longNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.longNumber *= v2.doubleNumber;
								break;
						}
						break;

					case FLOAT:
						switch (t2) {
							case CHAR:
								v1.floatNumber *= v2.character;
								break;

							case BYTE:
								v1.floatNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.floatNumber *= v2.shortNumber;
								break;

							case INT:
								v1.floatNumber *= v2.intNumber;
								break;

							case LONG:
								v1.floatNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.floatNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.floatNumber *= v2.doubleNumber;
								break;
						}
						break;

					case DOUBLE:
						switch (t2) {
							case CHAR:
								v1.doubleNumber *= v2.character;
								break;

							case BYTE:
								v1.doubleNumber *= v2.byteNumber;
								break;

							case SHORT:
								v1.doubleNumber *= v2.shortNumber;
								break;

							case INT:
								v1.doubleNumber *= v2.intNumber;
								break;

							case LONG:
								v1.doubleNumber *= v2.longNumber;
								break;

							case FLOAT:
								v1.doubleNumber *= v2.floatNumber;
								break;

							case DOUBLE:
								v1.doubleNumber *= v2.doubleNumber;
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
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
