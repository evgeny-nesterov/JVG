package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalAnd extends BinaryOperation {
	private static Operation instance = new OperationLogicalAnd();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalAnd() {
		super("&&", LOGICAL_AND);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;

		if (c1 == TYPE_BOOLEAN && c2 == TYPE_BOOLEAN) {
			v1.bool = v1.bool && v2.bool;
			return;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
