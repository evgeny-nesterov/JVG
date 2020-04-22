package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.fields.FieldPrimitive;

public class OperationPrefixPlus extends UnaryOperation {
	private static Operation instance = new OperationPrefixPlus();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixPlus() {
		super("+", PREFIX_PLUS);
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
				v.type = TYPE_INT;
				v.intNumber = v.character;
				break;

			case BYTE:
				v.type = TYPE_INT;
				v.intNumber = v.byteNumber;
				break;

			case SHORT:
				v.type = TYPE_INT;
				v.intNumber = v.shortNumber;
				break;
		}
	}
}
