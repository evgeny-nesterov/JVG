package script.ool.model.fields;

import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public class FieldArray extends Field<Object> {
	public FieldArray(Type type, String name) {
		super(type, name);
	}

	public Object array;

	public Clazz arrayType;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		// check value on array and on object
		if (!value.type.isArray() && !(value.type.isObject() && value.type.superClass == null)) {
			ctx.throwException("array is expected");
			return;
		}

		Clazz type = getClazz(ctx);
		if (!autoCast(value.type, type)) {
			ctx.throwException("incompatible types; found " + type.getClassName() + ", required " + value.type.getClassName());
			return;
		}

		value.valueType = Value.VALUE;
		value.type = arrayType != null ? arrayType : getClazz(ctx);
		value.array = array;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		if (value.type == Clazz.getNullClass()) {
			array = null;
		} else if (!value.type.isArray()) {
			ctx.throwException("array is expected");
			return;
		} else {
			// check cast
			Clazz type = getClazz(ctx);
			if (!autoCast(value.type, type)) {
				ctx.throwException("incompatible types; found " + value.type.getClassName() + ", required " + type.getClassName());
				return;
			}

			array = value.array;
			arrayType = value.type;
		}
		initialized = true;
	}

	@Override
	public Object get() {
		return array;
	}
}
