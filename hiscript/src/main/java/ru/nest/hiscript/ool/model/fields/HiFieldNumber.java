package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public abstract class HiFieldNumber<T> extends HiFieldPrimitive<T> {
	public HiFieldNumber(String typeName, String name) {
		super(Type.getPrimitiveType(typeName), name);
	}

	@Override
	public final void get(RuntimeContext ctx, Value value) {
		if (!value.type.isPrimitive()) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		int valueType = getType(value.type);
		if (valueType == BOOLEAN) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		get(ctx, value, valueType);

		value.valueType = Value.VALUE;
		value.type = getClass(ctx);
	}

	public abstract void get(RuntimeContext ctx, Value value, int valueType);

	@Override
	public final void set(RuntimeContext ctx, Value value) {
		if (!value.type.isPrimitive()) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		int valueType = getType(value.type);
		if (valueType == BOOLEAN) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		if (initialized && getModifiers().isFinal()) {
			ctx.throwException("cannot assign a value to final variable " + name);
			return;
		}

		set(ctx, value, valueType);
		initialized = true;
	}

	public abstract void set(RuntimeContext ctx, Value value, int valueType);
}
