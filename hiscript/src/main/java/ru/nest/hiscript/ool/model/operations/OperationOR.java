package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.FieldPrimitive;

public class OperationOR extends BinaryOperation {
	private static Operation instance = new OperationOR();

	public static Operation getInstance() {
		return instance;
	}

	private OperationOR() {
		super("|", OR);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;

		boolean isP1 = c1.isPrimitive();
		boolean isP2 = c2.isPrimitive();
		if (!isP1 || !isP2) {
			errorInvalidOperator(ctx, c1, c2);
			return;
		}

		int t1 = FieldPrimitive.getType(c1);
		int t2 = FieldPrimitive.getType(c2);
		switch (t1) {
			case BOOLEAN:
				if (t2 == BOOLEAN) {
					v1.bool = v1.bool | v2.bool;
					return;
				}
				break;

			case CHAR:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character | v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character | v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character | v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.character | v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.character | v2.longNumber;
						return;
				}
				break;

			case BYTE:
				switch (t2) {
					case CHAR:
						v1.intNumber = v1.byteNumber | v2.character;
						v1.type = TYPE_INT;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber | v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber | v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.byteNumber | v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.byteNumber | v2.longNumber;
						return;
				}
				break;

			case SHORT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber | v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber | v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber | v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.shortNumber | v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.shortNumber | v2.longNumber;
						return;
				}
				break;

			case INT:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber | v2.character;
						return;

					case BYTE:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber | v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber | v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_INT;
						v1.intNumber = v1.intNumber | v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.intNumber | v2.longNumber;
						return;
				}
				break;

			case LONG:
				switch (t2) {
					case CHAR:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber | v2.character;
						return;

					case BYTE:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber | v2.byteNumber;
						return;

					case SHORT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber | v2.shortNumber;
						return;

					case INT:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber | v2.intNumber;
						return;

					case LONG:
						v1.type = TYPE_LONG;
						v1.longNumber = v1.longNumber | v2.longNumber;
						return;
				}
				break;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
