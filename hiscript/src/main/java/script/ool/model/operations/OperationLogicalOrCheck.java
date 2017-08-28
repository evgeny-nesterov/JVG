package script.ool.model.operations;

import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class OperationLogicalOrCheck extends UnaryOperation {
	private static Operation instance = new OperationLogicalOrCheck();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalOrCheck() {
		super("?||", LOGICAL_OR_CHECK);
	}

	public void doOperation(RuntimeContext ctx, Value v) {
	}
}
