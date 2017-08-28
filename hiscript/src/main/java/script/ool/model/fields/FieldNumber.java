package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public abstract class FieldNumber<T> extends FieldPrimitive<T> {
	public FieldNumber(String typeName, String name) {
		super(Type.getPrimitiveType(typeName), name);
	}

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
		value.type = getClazz(ctx);
	}

	public abstract void get(RuntimeContext ctx, Value value, int valueType);

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
