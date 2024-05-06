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

public class HiFieldObject extends HiField<Object> {
	public HiFieldObject(Type type, String name) {
		super(type, name);
	}

	public HiFieldObject(Type type, String name, Object object) {
		super(type, name);
		this.object = object;
		this.initialized = true;
	}

	public HiFieldObject(HiClass clazz, String name) {
		super(clazz, name);
	}

	private Object object; // HiObject or array (if object class is Object)

	private HiClass lambdaClass; // has to be different from object class

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		HiClass valueClass = valueType.clazz;

		// autobox
		if (valueClass.isPrimitive() && valueClass != HiClassPrimitive.VOID) {
			valueClass = valueClass.getAutoboxClass();
		}
		return valueClass.isNull() || (valueClass.isObject() && valueClass.isInstanceof(fieldClass));
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.valueClass = getClass(ctx);
		value.lambdaClass = lambdaClass;
		value.object = object;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;
		if (value.valueClass.isNull()) {
			object = null;
			lambdaClass = null;
		} else {
			// generic
			if (value.valueClass.isGeneric() && value.object != null) {
				if (value.object instanceof HiObject) {
					HiClass dstClass = getClass(ctx);
					HiClass srcClass = ((HiObject) value.object).clazz;
					if (!srcClass.isInstanceof(dstClass)) {
						ctx.throwRuntimeException("cannot convert '" + srcClass.getNameDescr() + "' to '" + dstClass.getNameDescr() + "'");
					}
				} else {
					// TODO array
				}
			}
			lambdaClass = value.valueClass.isLambda() ? value.valueClass : null;
			object = value.getObject(getClass(ctx));
		}
		initialized = true;
	}

	@Override
	public Object get() {
		return object;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		if (object instanceof HiObject) {
			return ((HiObject) object).getJavaValue(ctx);
		} else {
			return HiFieldArray.getJava(ctx, getClass(ctx), object);
		}
	}

	public void set(Object object) {
		this.declared = true;
		this.object = object;
		this.lambdaClass = null;
		this.initialized = true;
	}

	public static HiFieldObject createStringField(RuntimeContext ctx, String name, String value) {
		return new HiFieldObject(Type.stringType, name, NodeString.createString(ctx, value));
	}
}
