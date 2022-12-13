package ru.nest.hiscript.ool.model.classes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiConstructorJava;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiFieldJava;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiMethodJava;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class HiClassJava extends HiClass {
	public Class javaClass;

	public HiClassJava(String name, Class javaClass) {
		super(null, null, name, CLASS_TYPE_TOP);
		this.javaClass = javaClass;
	}

	@Override
	protected HiConstructor _searchConstructor(RuntimeContext ctx, HiClass[] argTypes) {
		Class[] javaArgClasses = new Class[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			HiClass argType = argTypes[i];
			if (argType.isNull()) {
				continue;
			}
			Class argTypeJavaClass = argType.getJavaClass();
			if (argTypeJavaClass == null) {
				ctx.throwRuntimeException("Inconvertible java class argument: " + argType.fullName);
				return null;
			}
			javaArgClasses[i] = argTypeJavaClass;
		}
		// TODO argType may be null
		try {
			Constructor constructor = javaClass.getConstructor(javaArgClasses);
			return new HiConstructorJava(this, constructor);
		} catch (Exception e) {
			ctx.throwRuntimeException(e.getMessage());
			return null;
		}
	}

	@Override
	protected HiMethod _searchMethod(RuntimeContext ctx, String name, HiClass... argTypes) {
		Class[] javaArgClasses = new Class[argTypes.length];
		for (int i = 0; i < argTypes.length; i++) {
			HiClass argType = argTypes[i];
			if (argType.isNull()) {
				continue;
			}
			Class argTypeJavaClass = argType.getJavaClass();
			if (argTypeJavaClass == null) {
				ctx.throwRuntimeException("Inconvertible java class argument: " + argType.fullName);
				return null;
			}
			javaArgClasses[i] = argTypeJavaClass;
		}
		// TODO argType may be null
		try {
			Method method = javaClass.getMethod(name, javaArgClasses);
			return new HiMethodJava(this, method, name);
		} catch (NoSuchMethodException e) {
			ctx.throwRuntimeException(e.getMessage());
			return null;
		}
	}

	@Override
	protected HiField<?> _searchField(RuntimeContext ctx, String name) {
		try {
			Field field = javaClass.getDeclaredField(name);
			//if (field.isAccessible()) {
			return new HiFieldJava(field, name);
			//}
		} catch (NoSuchFieldException e) {
			ctx.throwRuntimeException(e.getMessage());
		}
		return null;
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isJava() {
		return true;
	}

	@Override
	public void code(CodeContext os) throws IOException {
	}

	public static HiClass decode(DecodeContext os) throws IOException {
		return null;
	}

	@Override
	public Class getJavaClass() {
		return javaClass;
	}
}
