package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassGeneric;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

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

	private Object object; // HiObject or array (if object class is Object, then use valueClass to define array value type)

	@Override
	public HiClass getValueClass(ClassResolver classResolver) {
		if (object instanceof HiObject) {
			return ((HiObject) object).clazz;
		}
		return super.getValueClass(classResolver);
	}

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		HiClass valueClass = valueType.clazz;

		// @lambda
		if (valueClass.isLambda()) {
			this.valueClass = valueClass;
			return true;
		}

		// @autoboxing
		if (valueClass.isPrimitive() && valueClass != HiClassPrimitive.VOID) {
			valueClass = valueClass.getAutoboxClass();
		}
		return valueClass.isNull() || (valueClass.isObject() && valueClass.isInstanceof(fieldClass));
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.setObjectOrArrayValue(getClass(ctx), valueClass, object);
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;
		if (value.valueClass.isNull()) {
			object = null;
			valueClass = null;
		} else {
			// @generics
			if (!value.valueClass.isPrimitive() && !value.valueClass.isLambda() && value.object != null) {
				if (value.object instanceof HiObject) {
					HiClass dstClass = getClass(ctx);
					HiClass srcClass = ((HiObject) value.object).clazz;

					// @generics
					if (dstClass.isGeneric()) {
						dstClass = ((HiClassGeneric) dstClass).clazz;
					}

					if (!srcClass.isInstanceof(dstClass)) {
						ctx.throwRuntimeException("cannot convert '" + srcClass.getNameDescr() + "' to '" + dstClass.getNameDescr() + "'");
					}
				} else {
					// TODO array
				}
			}
			valueClass = value.valueClass;
			object = value.getObject(getClass(ctx));

			// @generic
			if (valueClass.isGeneric() && object instanceof HiObject) {
				valueClass = ((HiObject) object).clazz;
			}
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
			// array
			return HiFieldArray.getJava(ctx, getClass(ctx), object);
		}
	}

	@Override
	public void set(Object object, HiClass valueClass) {
		super.set(valueClass, valueClass);
		this.object = object;
	}
}
