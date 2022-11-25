package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationPrefixExclamation extends UnaryOperation {
	private static Operation instance = new OperationPrefixExclamation();

	public static Operation getInstance() {
		return instance;
	}

	private OperationPrefixExclamation() {
		super("!", PREFIX_EXCLAMATION);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v) {
		HiClass c = v.type;
		if (c != TYPE_BOOLEAN) {
			errorInvalidOperator(ctx, c);
			return;
		}

		v.bool = !v.bool;
	}
}
