package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeIdentificator;

public class OperationTriger extends Operation {
	private static Operation instance = new OperationTriger();

	public static Operation getInstance() {
		return instance;
	}

	private OperationTriger() {
		super("?:", 3, TRIGER);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value... v) {
		if (v[0].valueType == Value.NAME) {
			NodeIdentificator.resolve(ctx, v[0], true);
		}

		if (v[1].valueType == Value.NAME) {
			NodeIdentificator.resolve(ctx, v[1], true);
		}

		if (v[2].valueType == Value.NAME) {
			NodeIdentificator.resolve(ctx, v[2], true);
		}

		if (v[0].getBoolean()) {
			v[1].copyTo(v[0]);
		} else {
			v[2].copyTo(v[0]);
		}
	}
}
