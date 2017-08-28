package script.ool.model.operations;

import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class SkipOperation extends Operation {
	private static Operation instance = new SkipOperation();

	public static Operation getInstance() {
		return instance;
	}

	private SkipOperation() {
		super("->", 0, SKIP);
	}

	public void doOperation(RuntimeContext ctx, Value... v) {
	}
}
