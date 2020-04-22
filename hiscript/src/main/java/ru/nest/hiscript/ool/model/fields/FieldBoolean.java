package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public class FieldBoolean extends FieldPrimitive<Boolean> {
	public FieldBoolean(String name) {
		super(Type.getPrimitiveType("boolean"), name);
	}

	private boolean value;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.type = getClazz(ctx);
		value.bool = this.value;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		if (value.type != getClazz(ctx)) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		this.value = value.bool;
	}

	@Override
	public Boolean get() {
		return value;
	}
}
