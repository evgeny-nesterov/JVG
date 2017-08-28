package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public class FieldBoolean extends FieldPrimitive<Boolean> {
	public FieldBoolean(String name) {
		super(Type.getPrimitiveType("boolean"), name);
	}

	private boolean value;

	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.type = getClazz(ctx);
		value.bool = this.value;
	}

	public void set(RuntimeContext ctx, Value value) {
		if (value.type != getClazz(ctx)) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		this.value = value.bool;
	}

	public Boolean get() {
		return value;
	}
}
