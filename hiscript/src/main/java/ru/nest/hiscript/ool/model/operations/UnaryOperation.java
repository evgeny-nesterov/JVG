package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeIdentifier;

public abstract class UnaryOperation extends Operation {
	UnaryOperation(String name, int operation) {
		super(name, 1, operation);
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v = values[0];

		if (v.valueType == Value.NAME) {
			NodeIdentifier.resolve(ctx, v, true);
		}

		doOperation(ctx, v);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v);

	public void errorInvalidOperator(RuntimeContext ctx, HiClass type) {
		// operator '<operator>' can not be applied to <type>
	}
}
