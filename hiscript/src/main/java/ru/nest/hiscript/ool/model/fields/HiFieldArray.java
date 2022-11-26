package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldArray extends HiField<Object> {
	public HiFieldArray(Type type, String name) {
		super(type, name);
	}

	public Object array;

	public HiClass arrayType;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		// check value on array and on object
		if (!value.type.isArray() && !(value.type.isObject() && value.type.superClass == null)) {
			ctx.throwRuntimeException("array is expected");
			return;
		}

		HiClass type = getClass(ctx);
		if (!autoCast(value.type, type)) {
			ctx.throwRuntimeException("incompatible types; found " + type.getClassName() + ", required " + value.type.getClassName());
			return;
		}

		value.valueType = Value.VALUE;
		value.type = arrayType != null ? arrayType : getClass(ctx);
		value.array = array;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		if (value.type == HiClass.getNullClass()) {
			array = null;
		} else if (!value.type.isArray()) {
			ctx.throwRuntimeException("array is expected");
			return;
		} else {
			// check cast
			HiClass type = getClass(ctx);
			if (!autoCast(value.type, type)) {
				ctx.throwRuntimeException("incompatible types; found " + value.type.getClassName() + ", required " + type.getClassName());
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
