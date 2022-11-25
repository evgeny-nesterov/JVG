package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalSwitch extends Operation {
	private static Operation instance = new OperationLogicalSwitch();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalSwitch() {
		super("?", 1, LOGICAL_SWITCH);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value... v) {
	}
}
