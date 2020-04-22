package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class SkipOperation extends Operation {
	private static Operation instance = new SkipOperation();

	public static Operation getInstance() {
		return instance;
	}

	private SkipOperation() {
		super("->", 0, SKIP);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value... v) {
	}
}
