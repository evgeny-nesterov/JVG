package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalAndCheck extends UnaryOperation {
	private static Operation instance = new OperationLogicalAndCheck();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalAndCheck() {
		super("?&&", LOGICAL_AND_CHECK);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
	}
}
