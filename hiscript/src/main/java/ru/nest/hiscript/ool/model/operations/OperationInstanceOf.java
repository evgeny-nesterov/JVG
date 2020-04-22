package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationInstanceOf extends BinaryOperation {
	private static Operation instance = new OperationInstanceOf();

	public static Operation getInstance() {
		return instance;
	}

	private OperationInstanceOf() {
		super("instanceof", INSTANCEOF);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;
		v1.type = TYPE_BOOLEAN;

		if (!c1.isPrimitive()) {
			v1.bool = c1.isInstanceof(c2);
			return;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
