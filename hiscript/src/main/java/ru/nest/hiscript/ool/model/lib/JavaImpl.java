package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassJava;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class JavaImpl extends ImplUtil {
	public static void Java_void_importClass_String_String(RuntimeContext ctx, HiObject className, HiObject javaClassName) throws ClassNotFoundException {
		String name = getString(ctx, className);
		String javaName = getString(ctx, javaClassName);
		HiClass clazz = ctx.getClassLoader().getClass(name);
		if (clazz == null) {
			Class javaClass = Class.forName(javaName);
			clazz = new HiClassJava(ctx.getClassLoader(), name, javaClass);
		} else if (!(clazz instanceof HiClassJava)) {
			ctx.throwRuntimeException("cannot import java class with name " + className);
		}
		returnVoid(ctx);
	}

	public static void Java_void_newInstance_String_0Object(RuntimeContext ctx, HiObject className, Object[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
		String javaClassName = getString(ctx, className);
		Class javaClass = Class.forName(javaClassName);
		String hiClassName = "#" + javaClassName.replace('.', '_');

		HiClass clazz = ctx.getClassLoader().getClass(hiClassName);
		if (clazz == null) {
			clazz = new HiClassJava(ctx.getClassLoader(), hiClassName, javaClass);
		} else if (!clazz.isJava()) {
			ctx.throwRuntimeException("cannot import java class with name " + hiClassName);
		}

		Object[] javaArgs = new Object[args.length];
		Class[] javaArgsClasses = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof HiObject) {
				javaArgs[i] = ((HiObject) args[i]).getJavaValue(ctx);
			} else {
				javaArgs[i] = args[i];
			}
			javaArgsClasses[i] = javaArgs[i] != null ? javaArgs[i].getClass() : null;
		}
		Constructor javaConstructor = javaClass.getConstructor(javaArgsClasses);
		Object javaObject = javaConstructor.newInstance(javaArgs);

		HiObject object = new HiObject(clazz, null);
		object.userObject = javaObject;

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazz;
		ctx.value.object = object;
	}
}