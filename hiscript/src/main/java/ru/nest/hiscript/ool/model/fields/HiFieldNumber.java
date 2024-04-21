package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

public abstract class HiFieldNumber<T> extends HiFieldPrimitive<T> {
	public HiFieldNumber(String typeName, String name) {
		super(Type.getPrimitiveType(typeName), name);
	}

	@Override
	public final void get(RuntimeContext ctx, Value value) {
		HiClass valueClass = value.getOperationClass();
		if (!valueClass.isPrimitive()) {
			ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
			return;
		}

		int valueType = valueClass.getPrimitiveType();
		if (valueType == BOOLEAN) {
			ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
			return;
		}

		get(ctx, value, valueType);

		value.valueType = Value.VALUE;
		value.valueClass = getClass(ctx);
	}

	public abstract void get(RuntimeContext ctx, Value value, int valueType);

	@Override
	public final void set(RuntimeContext ctx, Value value) {
		declared = true;
		if (value.valueClass.getAutoboxedPrimitiveClass() != null) {
			// autobox
			value.substitutePrimitiveValueFromAutoboxValue();
		} else if (!value.valueClass.isPrimitive()) {
			ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
			return;
		}

		int valueType = HiFieldPrimitive.getAutoType(value.valueClass);
		if (valueType == BOOLEAN) {
			ctx.throwRuntimeException("incompatible types; found " + value.valueClass.getNameDescr() + ", required " + type.fullName);
			return;
		}

		if (initialized && getModifiers().isFinal()) {
			ctx.throwRuntimeException("cannot assign a value to final variable " + name);
			return;
		}

		set(ctx, value, valueType);
		initialized = true;
	}

	public abstract void set(RuntimeContext ctx, Value value, int valueType);
}
