package script.ool.model.operations;

import script.ool.model.Arrays;
import script.ool.model.Field;
import script.ool.model.Operation;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class OperationEquate extends BinaryOperation {
	private static Operation instance = new OperationEquate();

	public static Operation getInstance() {
		return instance;
	}

	private OperationEquate() {
		super("=", EQUATE);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		if (v2.valueType == Value.VARIABLE) {
			if (!v2.variable.initialized) {
				ctx.throwException("variable not initialized: " + v2.variable.name);
				return;
			}
		}

		if (v1.valueType == Value.VARIABLE) {
			// 1. copy variable from v1
			Field<?> variable = v1.variable;

			// 2. copy v2 to v1
			v2.copyTo(v1);

			// 3. set v1 variable again
			v1.valueType = Value.VARIABLE;
			v1.variable = variable;

			// 4. set value of variable from v2
			variable.set(ctx, v2);
			variable.initialized = true;

			// DEBUG
			// System.out.println(v1.variable.name + " (" + v1.variable.getClazz(ctx)+ ") = " + v1.variable.get() + ", " + v1.get() + ", " +
			// v1.type);
		} else if (v1.valueType == Value.ARRAY_INDEX) {
			if (!Field.autoCast(v2.type, v1.type)) {
				ctx.throwException("incompatible types; found " + v2.type + ", required " + v1.type);
				return;
			}
			Arrays.setArrayIndex(v1.type, v1.parentArray, v1.arrayIndex, v2, v1);
		} else {
			errorUnexpectedType(ctx);
		}
	}
}
