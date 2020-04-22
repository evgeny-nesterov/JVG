package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public class FieldObject extends Field<Obj> {
	public FieldObject(Type type, String name) {
		super(type, name);
	}

	private Obj object;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		Clazz clazz = getClazz(ctx);
		if (!clazz.isNull() && (!value.type.isObject() || !clazz.isInstanceof(value.type))) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + clazz.fullName);
			return;
		}

		value.valueType = Value.VALUE;
		value.type = clazz;
		value.object = object;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		Clazz valueClazz = value.type;
		if (valueClazz.isNull()) {
			object = null;
			return;
		}

		Clazz clazz = getClazz(ctx);
		if (!valueClazz.isObject() || !valueClazz.isInstanceof(clazz)) {
			ctx.throwException("incompatible types; found " + valueClazz.fullName + ", required " + clazz.fullName);
			return;
		}

		object = value.object;
	}

	@Override
	public Obj get() {
		return object;
	}

	public void set(Obj object) {
		this.object = object;
	}
}
