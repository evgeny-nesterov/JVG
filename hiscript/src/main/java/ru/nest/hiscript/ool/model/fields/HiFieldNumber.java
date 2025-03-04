package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

public abstract class HiFieldNumber<T> extends HiFieldPrimitive<T> {
	public HiFieldNumber(String typeName, String name) {
		super(Type.getPrimitiveType(typeName), name);
	}

	@Override
	public final void get(RuntimeContext ctx, Value value) {
		getPrimitiveValue(ctx, value);

		// after get
		value.valueType = ValueType.VALUE;
		value.valueClass = getClass(ctx);
	}

	public abstract void getPrimitiveValue(RuntimeContext ctx, Value value);

	@Override
	public final void set(RuntimeContext ctx, Value value) {
		declared = true;

		// @autoboxing
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
