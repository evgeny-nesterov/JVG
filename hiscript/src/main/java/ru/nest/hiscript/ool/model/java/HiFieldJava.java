package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.Value;

import java.lang.reflect.Field;

public class HiFieldJava extends HiField {
	private Field field;

	public HiFieldJava(Field field, String name) {
		super((Type) null, name);
		this.field = field;
		field.setAccessible(true);
		type = HiJava.getTypeByJavaClass(field.getType());
		declared = true;
		initialized = true;
	}

	@Override
	public boolean isStatic() {
		return java.lang.reflect.Modifier.isStatic(field.getModifiers());
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		if (value.object.userObject == null) {
			ctx.throwRuntimeException("Null pointer");
			return;
		}

		try {
			Object resultJavaValue = field.get(value.object.userObject);
			Object resultValue = HiJava.convertFromJava(ctx, resultJavaValue);
			value.set(resultValue);
		} catch (IllegalAccessException e) {
			ctx.throwRuntimeException(e.toString());
		}
	}

	@Override
	public Object get() {
		return null;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return null;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
	}
}
