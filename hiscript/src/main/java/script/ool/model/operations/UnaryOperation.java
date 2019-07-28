package script.ool.model.operations;

import script.ool.model.Clazz;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.nodes.NodeIdentificator;

public abstract class UnaryOperation extends Operation {
	UnaryOperation(String name, int operation) {
		super(name, 1, operation);
	}

	@Override
	public final void doOperation(RuntimeContext ctx, Value... values) {
		Value v = values[0];

		if (v.valueType == Value.NAME) {
			NodeIdentificator.resolve(ctx, v, true);
		}

		doOperation(ctx, v);
	}

	public abstract void doOperation(RuntimeContext ctx, Value v);

	public void errorInvalidOperator(RuntimeContext ctx, Clazz type) {
		// operator '<operator>' can not be applyed to <type>
	}
}
