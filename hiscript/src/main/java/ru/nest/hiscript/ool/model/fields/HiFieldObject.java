package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class HiFieldObject extends HiField<HiObject> {
	public HiFieldObject(Type type, String name) {
		super(type, name);
	}

	public HiFieldObject(Type type, String name, HiObject object) {
		super(type, name);
		this.object = object;
		this.initialized = true;
	}

	private HiObject object;

	@Override
	public void get(RuntimeContext ctx, Value value) {
		HiClass clazz = getClass(ctx);
		if (!clazz.isNull() && (!value.type.isObject() || !clazz.isInstanceof(value.type))) {
			ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + clazz.fullName);
			return;
		}

		value.valueType = Value.VALUE;
		value.type = clazz;
		value.object = object;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;

		HiClass valueClass = value.type;
		if (valueClass.isNull()) {
			object = null;
			return;
		}

		HiClass clazz = getClass(ctx);
		if (ctx.exception != null) {
			return;
		}
		if (!valueClass.isObject() || !valueClass.isInstanceof(clazz)) {
			ctx.throwRuntimeException("incompatible types; found " + valueClass.fullName + ", required " + clazz.fullName);
			return;
		}

		object = value.object;
		initialized = true;
	}

	@Override
	public HiObject get() {
		return object;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return object.getJavaValue(ctx);
	}

	public void set(HiObject object) {
		declared = true;
		this.object = object;
		initialized = true;
	}

	public static HiFieldObject createStringField(RuntimeContext ctx, String name, String value) {
		return new HiFieldObject(Type.stringType, "name", NodeString.createString(ctx, value.toCharArray()));
	}
}
