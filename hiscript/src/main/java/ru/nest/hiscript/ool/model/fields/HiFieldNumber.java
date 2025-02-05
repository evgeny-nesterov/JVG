package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public abstract class HiFieldNumber<T> extends HiFieldPrimitive<T> {
	public HiFieldNumber(String typeName, String name) {
		super(Type.getPrimitiveType(typeName), name);
	}

	@Override
	public final void get(RuntimeContext ctx, Value value) {
		HiClass valueClass = value.getOperationClass();
		int valueType = valueClass.getPrimitiveType();

		get(ctx, value, valueType);

		value.valueType = Value.VALUE;
		value.valueClass = getClass(ctx);
	}

	public abstract void get(RuntimeContext ctx, Value value, int valueType);

	@Override
	public final void set(RuntimeContext ctx, Value value) {
		declared = true;

		// autobox
		if (value.valueClass.getAutoboxedPrimitiveClass() != null) {
			value.substitutePrimitiveValueFromAutoboxValue();
			if (ctx.exitFromBlock()) {
				return;
			}
		}

		int valueType = HiFieldPrimitive.getAutoType(value.valueClass);
		set(ctx, value, valueType);
		initialized = true;
	}

	public abstract void set(RuntimeContext ctx, Value value, int valueType);
}
