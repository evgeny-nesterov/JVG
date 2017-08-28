package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class OperationPrefixExclamation extends UnaryOperation {
	private static Operation instance = new OperationPrefixExclamation();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixExclamation() {
		super("!", PREFIX_EXCLAMATION);
	}

	public void doOperation(RuntimeContext ctx, Value v) {
		Clazz c = v.type;
		if (c != TYPE_BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		v.bool = !v.bool;
	}
}
