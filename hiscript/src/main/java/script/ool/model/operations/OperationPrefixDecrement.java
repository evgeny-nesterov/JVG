package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;

public class OperationPrefixDecrement extends UnaryOperation {
	private static Operation instance = new OperationPrefixDecrement();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixDecrement() {
		super("--", PREFIX_DECREMENT);
	}

	@Override
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
				v.character--;
				break;

			case BYTE:
				v.byteNumber--;
				break;

			case SHORT:
				v.shortNumber--;
				break;

			case INT:
				v.intNumber--;
				break;

			case LONG:
				v.longNumber--;
				break;

			case FLOAT:
				v.floatNumber--;
				break;

			case DOUBLE:
				v.doubleNumber--;
				break;
		}

		Field<?> var = v.variable;
		var.set(ctx, v);
	}
}
