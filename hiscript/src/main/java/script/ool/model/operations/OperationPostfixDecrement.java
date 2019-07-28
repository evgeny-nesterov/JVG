package script.ool.model.operations;

import script.ool.model.Arrays;
import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.fields.FieldPrimitive;

public class OperationPostfixDecrement extends UnaryOperation {
	private static Operation instance = new OperationPostfixDecrement();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPostfixDecrement() {
		super("--", POST_DECREMENT);
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

		Field<?> var = v.variable;
		Value[] vs = ctx.getValues(1);
		try {
			Value tmp = vs[0];
			v.copyTo(tmp);

			switch (t) {
				case CHAR:
					tmp.character--;
					break;

				case BYTE:
					tmp.byteNumber--;
					break;

				case SHORT:
					tmp.shortNumber--;
					break;

				case INT:
					tmp.intNumber--;
					break;

				case LONG:
					tmp.longNumber--;
					break;

				case FLOAT:
					tmp.floatNumber--;
					break;

				case DOUBLE:
					tmp.doubleNumber--;
					break;
			}

			if (v.valueType == Value.ARRAY_INDEX) {
				Arrays.setArrayIndex(v.type, v.parentArray, v.arrayIndex, tmp, ctx.value);
			} else {
				var.set(ctx, tmp);
			}
		} finally {
			ctx.putValues(vs);
		}
	}
}
