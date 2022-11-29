package ru.nest.hiscript.ool.model.operations;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Operation;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.fields.HiFieldObject;

public class OperationInstanceOf extends BinaryOperation {
	private static Operation instance = new OperationInstanceOf();

	public static Operation getInstance() {
		return instance;
	}

	private OperationInstanceOf() {
		super("instanceof", INSTANCE_OF);
	}

	@Override
	public void doOperation(RuntimeContext ctx, Value v1, Value v2) {
		HiClass c2 = v2.type;
		if (!v1.type.isPrimitive()) {
			HiClass c1 = v1.object != null ? v1.object.clazz : HiClassNull.NULL;
			boolean isInstanceof = c1.isInstanceof(c2);
			if (isInstanceof && v2.castedVariableName != null) {
				if (ctx.getVariable(v2.castedVariableName) != null) {
					ctx.throwRuntimeException("Variable '" + v2.castedVariableName + "' is already defined in the scope");
					return;
				}

				HiFieldObject castedField = (HiFieldObject) HiField.getField(Type.getType(c2), v2.castedVariableName);
				castedField.set(v1.object);
				ctx.addVariable(castedField);
			}

			v1.type = TYPE_BOOLEAN;
			v1.bool = isInstanceof;
			return;
		}
		ctx.throwRuntimeException("Inconvertible types; cannot cast " + v1.type.fullName + " to " + c2.fullName);
	}
}
