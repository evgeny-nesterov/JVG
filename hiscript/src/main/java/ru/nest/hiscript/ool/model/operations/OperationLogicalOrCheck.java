package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalOrCheck extends UnaryOperation {
	private static Operation instance = new OperationLogicalOrCheck();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalOrCheck() {
		super("?||", LOGICAL_OR_CHECK);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
	}
}
