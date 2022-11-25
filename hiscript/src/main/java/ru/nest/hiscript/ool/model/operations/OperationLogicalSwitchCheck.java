package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalSwitchCheck extends Operation {
	private static Operation instance = new OperationLogicalSwitchCheck();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalSwitchCheck() {
		super("?:", 1, LOGICAL_SWITCH_CHECK);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value... v) {
	}
}
