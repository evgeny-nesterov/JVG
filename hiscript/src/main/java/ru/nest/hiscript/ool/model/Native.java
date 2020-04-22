package ru.nest.hiscript.ool.model;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public class Native {
	private static HashMap<String, java.lang.reflect.Method> methods = new HashMap<String, Method>();

	private static HashMap<String, Object> objects = new HashMap<String, Object>();

	private static HashSet<Class<?>> registered = new HashSet<Class<?>>();

	public static void register(Object o) {
		if (o != null) {
			register(o.getClass(), o);
		}
	}

	public static void register(Class<? extends Object> c) {
		if (c != null) {
			register(c, null);
		}
	}

	private static void register(Class<? extends Object> c, Object o) {
		if (registered.contains(c)) {
			return;
		}

		java.lang.reflect.Method[] methods = c.getMethods();
		for (java.lang.reflect.Method m : methods) {
			String name = m.getName();
			Class<?>[] argClasses = m.getParameterTypes();
			if (argClasses.length > 0 && argClasses[0] == RuntimeContext.class) {
				Native.methods.put(name, m);

				if (java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
					objects.put(name, c);
				} else {
					objects.put(name, o);
				}
			}
		}

		registered.add(c);
	}

	public static void invoke(RuntimeContext ctx, String name, Object[] args) {
		try {
			java.lang.reflect.Method method = methods.get(name);
			Object o = objects.get(name);
			method.invoke(o, args);
		} catch (Exception exc) {
			exc.printStackTrace();
			ctx.throwException("native method '" + name + "' invocation error: " + exc.toString());
		}
	}
}
