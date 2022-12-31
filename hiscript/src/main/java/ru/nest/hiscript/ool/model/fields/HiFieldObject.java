package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiFieldObject extends HiField<HiObject> {
	public HiFieldObject(Type type, String name) {
		super(type, name);
	}

	public HiFieldObject(Type type, String name, HiObject object) {
		super(type, name);
		this.object = object;
		this.initialized = true;
	}

	public HiFieldObject(HiClass clazz, String name) {
		super(clazz, name);
	}

	public HiFieldObject(HiClass clazz, String name, HiObject object) {
		super(clazz, name);
		this.object = object;
		this.initialized = true;
	}

	private HiObject object;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return valueType.type.isNull() || (valueType.type.isObject() && valueType.type.isInstanceof(fieldClass));
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.type = getClass(ctx);
		value.object = object;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;
		HiClass valueClass = value.type;
		if (valueClass.isNull()) {
			object = null;
		} else {
			object = value.object;
		}
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
		return new HiFieldObject(Type.stringType, name, NodeString.createString(ctx, value));
	}
}
