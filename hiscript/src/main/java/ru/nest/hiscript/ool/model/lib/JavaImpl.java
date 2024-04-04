package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassJava;

import java.lang.reflect.Constructor;

public class JavaImpl extends ImplUtil {
	public static void Java_void_importClass_String_String(RuntimeContext ctx, HiObject className, HiObject javaClassName) throws Exception {
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

	public static void Java_Object_newInstance_Class_String_0Object(RuntimeContext ctx, HiObject hiInterface, HiObject className, Object[] args) throws Exception {
		String javaClassName = getString(ctx, className);
		Class javaClass = Class.forName(javaClassName);
		String hiClassName = "#" + javaClassName.replace('.', '_');

		HiClass clazz = ctx.getClassLoader().getClass(hiClassName);
		if (clazz == null) {
			clazz = new HiClassJava(ctx.getClassLoader(), hiClassName, javaClass);
		} else if (!clazz.isJava()) {
			ctx.throwRuntimeException("cannot import java class '" + hiClassName + "'");
		}

		if (hiInterface != null && hiInterface.userObject instanceof HiClass) {
			clazz.interfaces = new HiClass[] {(HiClass) hiInterface.userObject};
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

		Constructor matchedJavaConstructor = null;
		for (Constructor javaConstructor : javaClass.getDeclaredConstructors()) {
			Class[] parameters = javaConstructor.getParameterTypes();
			boolean isVarArgs = javaConstructor.isVarArgs();
			if (isVarArgs) {

			} else if (parameters.length == javaArgsClasses.length) {
				boolean match = true;
				if (parameters.length > 0) {
					for (int i = 0; i < parameters.length; i++) {
						Class subType = javaArgsClasses[i];
						if (subType == null) {
							continue;
						}
						Class superType = parameters[i];
						if (subType == superType) {
							continue;
						}
						if (subType.isPrimitive() || superType.isPrimitive()) {
							if (subType == byte.class || subType == Byte.class) {
								if (superType == byte.class || superType == Byte.class) {
									continue;
								}
							} else if (subType == short.class || subType == Short.class) {
								if (superType == short.class || superType == Short.class) {
									continue;
								}
							} else if (subType == int.class || subType == Integer.class) {
								if (superType == int.class || superType == Integer.class || //
										superType == short.class || superType == Short.class || //
										superType == byte.class || superType == Byte.class) {
									continue;
								}
							} else if (subType == long.class || subType == Long.class) {
								if (superType == long.class || superType == Long.class || //
										superType == int.class || superType == Integer.class || //
										superType == short.class || superType == Short.class || //
										superType == byte.class || superType == Byte.class) {
									continue;
								}
							} else if (subType == float.class || subType == Float.class) {
								if (superType == float.class || superType == Float.class || //
										superType == int.class || superType == Integer.class || //
										superType == short.class || superType == Short.class || //
										superType == byte.class || superType == Byte.class) {
									continue;
								}
							} else if (subType == double.class || subType == Double.class) {
								if (superType == double.class || superType == Double.class || //
										superType == float.class || superType == Float.class || //
										superType == long.class || superType == Long.class || //
										superType == int.class || superType == Integer.class || //
										superType == short.class || superType == Short.class || //
										superType == byte.class || superType == Byte.class) {
									continue;
								}
							}
						} else if (superType.isAssignableFrom(subType)) {
							continue;
						}
						// TODO
						match = false;
						break;
					}
				} else {
					matchedJavaConstructor = javaConstructor;
					break;
				}
				if (match) {
					matchedJavaConstructor = javaConstructor;
					break;
				}
			}
		}

		if (matchedJavaConstructor != null) {
			Object javaObject = matchedJavaConstructor.newInstance(javaArgs);

			HiObject object = new HiObject(clazz, null);
			object.userObject = javaObject;

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = clazz;
			ctx.value.object = object;
		} else {
			ctx.throwRuntimeException("cannot find constructor for java class '" + hiClassName + "'");
		}
	}
}