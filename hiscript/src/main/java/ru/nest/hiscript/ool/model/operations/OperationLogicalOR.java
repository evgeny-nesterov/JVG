package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class OperationLogicalOR extends BinaryOperation {
	private static Operation instance = new OperationLogicalOR();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalOR() {
		super("||", LOGICAL_OR);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c1 = v1.type;
		HiClass c2 = v2.type;

		if (c1 == TYPE_BOOLEAN && c2 == TYPE_BOOLEAN) {
			v1.bool = v1.bool || v2.bool;
			return;
		}

		errorInvalidOperator(ctx, c1, c2);
	}
}
