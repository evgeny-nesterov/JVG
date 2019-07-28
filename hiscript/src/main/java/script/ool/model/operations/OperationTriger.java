package script.ool.model.operations;

import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.nodes.NodeIdentificator;

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
