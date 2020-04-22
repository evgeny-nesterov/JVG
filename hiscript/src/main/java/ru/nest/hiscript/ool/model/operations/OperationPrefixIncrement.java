package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.FieldPrimitive;

public class OperationPrefixIncrement extends UnaryOperation {
	private static Operation instance = new OperationPrefixIncrement();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixIncrement() {
		super("++", PREFIX_INCREMENT);
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
				v.character++;
				break;

			case BYTE:
				v.byteNumber++;
				break;

			case SHORT:
				v.shortNumber++;
				break;

			case INT:
				v.intNumber++;
				break;

			case LONG:
				v.longNumber++;
				break;

			case FLOAT:
				v.floatNumber++;
				break;

			case DOUBLE:
				v.doubleNumber++;
				break;
		}

		Field<?> var = v.variable;
		var.set(ctx, v);
	}
}
