package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class OperationLogicalOR extends BinaryOperation {
	private static Operation instance = new OperationLogicalOR();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalOR() {
		super("||", LOGICAL_OR);
	}

	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		Clazz c1 = v1.type;
		Clazz c2 = v2.type;

		if (c1 == TYPE_BOOLEAN && c2 == TYPE_BOOLEAN) {
			v1.bool = v1.bool || v2.bool;
			return;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
