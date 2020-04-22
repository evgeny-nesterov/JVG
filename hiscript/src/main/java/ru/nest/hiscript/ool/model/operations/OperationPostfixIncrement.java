package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Arrays;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.FieldPrimitive;

public class OperationPostfixIncrement extends UnaryOperation {
	private static Operation instance = new OperationPostfixIncrement();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPostfixIncrement() {
		super("++", POST_INCREMENT);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		Clazz c = v.type;

		boolean isPrimitive = c.isPrimitive();
		if (!isPrimitive) {
			errorInvalidOperator(ctx, c);
			return;
		}

		int type = FieldPrimitive.getType(c);
		if (type == BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		Field<?> var = v.variable;
		Value[] vs = ctx.getValues(1);
		try {
			Value tmp = vs[0];
			v.copyTo(tmp);

			switch (type) {
				case CHAR:
					tmp.character++;
					break;

				case BYTE:
					tmp.byteNumber++;
					break;

				case SHORT:
					tmp.shortNumber++;
					break;

				case INT:
					tmp.intNumber++;
					break;

				case LONG:
					tmp.longNumber++;
					break;

				case FLOAT:
					tmp.floatNumber++;
					break;

				case DOUBLE:
					tmp.doubleNumber++;
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
