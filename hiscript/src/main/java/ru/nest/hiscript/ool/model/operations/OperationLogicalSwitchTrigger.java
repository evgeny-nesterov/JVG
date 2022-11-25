package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeIdentificator;

public class OperationLogicalSwitchTrigger extends Operation {
	private static Operation instance = new OperationLogicalSwitchTrigger();

	public static Operation getInstance() {
		return instance;
	}

	private OperationLogicalSwitchTrigger() {
		super(":", 3, LOGICAL_SWITCH_TRIGGER);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value... v) {
		if (v[0].valueType == Value.NAME) {
			NodeIdentificator.resolve(ctx, v[0], true);
		}

		if (v[0].getBoolean()) {
			ctx.value = v[1];
			v[1].node.execute(ctx);
			if (v[1].valueType == Value.NAME) {
				NodeIdentificator.resolve(ctx, v[1], true);
			}
			ctx.value.copyTo(v[0]);
		} else {
			ctx.value = v[2];
			v[2].node.execute(ctx);
			if (v[2].valueType == Value.NAME) {
				NodeIdentificator.resolve(ctx, v[2], true);
			}
			ctx.value.copyTo(v[0]);
		}
	}
}
