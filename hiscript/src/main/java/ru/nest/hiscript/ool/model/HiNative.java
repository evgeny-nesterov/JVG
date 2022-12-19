package ru.nest.hiscript.ool.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiNative {
	private static Map<String, java.lang.reflect.Method> methods = new ConcurrentHashMap<>();

	private static Map<String, Object> objects = new ConcurrentHashMap<>();

	private static Set<Class<?>> registered = ConcurrentHashMap.newKeySet();

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
				HiNative.methods.put(name, m);

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
			if (method != null) {
				Object o = objects.get(name);
				method.invoke(o, args);
			} else {
				ctx.throwRuntimeException("native method '" + name + "' not found");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			ctx.throwRuntimeException("native method '" + name + "' invocation error: " + exc.toString());
		}
	}
}
