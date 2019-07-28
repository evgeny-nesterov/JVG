package script.ool.model.operations;

import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

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
