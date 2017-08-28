package script.ool.model.fields;

import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public class FieldObject extends Field<Obj> {
	public FieldObject(Type type, String name) {
		super(type, name);
	}

	private Obj object;

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

	public Obj get() {
		return object;
	}

	public void set(Obj object) {
		this.object = object;
	}
}
