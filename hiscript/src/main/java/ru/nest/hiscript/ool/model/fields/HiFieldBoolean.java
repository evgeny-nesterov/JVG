package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldBoolean extends HiFieldPrimitive<Boolean> {
	public HiFieldBoolean(String name) {
		super(Type.getPrimitiveType("boolean"), name);
	}

	private boolean value;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.type = getClass(ctx);
		value.bool = this.value;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		if (value.type != getClass(ctx)) {
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
