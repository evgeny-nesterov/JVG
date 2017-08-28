package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;

public class OperationPrefixMinus extends UnaryOperation {
	private static Operation instance = new OperationPrefixMinus();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixMinus() {
		super("-", PREFIX_MINUS);
	}

	public void doOperation(RuntimeContext ctx, Value v) {
		Clazz c = v.type;

		boolean isP = c.isPrimitive();
		if (!isP) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int t = FieldPrimitive.getType(c);
		if (t == BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		switch (t) {
			case CHAR:
				v.type = TYPE_INT;
				v.intNumber = -v.character;
				break;

			case BYTE:
				v.type = TYPE_INT;
				v.intNumber = -v.byteNumber;
				break;

			case SHORT:
				v.type = TYPE_INT;
				v.intNumber = -v.shortNumber;
				break;

			case INT:
				v.intNumber = -v.intNumber;
				break;

			case LONG:
				v.longNumber = -v.longNumber;
				break;

			case FLOAT:
				v.floatNumber = -v.floatNumber;
				break;

			case DOUBLE:
				v.doubleNumber = -v.doubleNumber;
				break;
		}
	}
}
