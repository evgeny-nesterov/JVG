package ru.nest.hiscript.ool.model;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiNative {
	private static final Map<String, Method> methods = new ConcurrentHashMap<>();

	private static final Map<String, Object> objects = new ConcurrentHashMap<>();

	private static final Set<Class<?>> registered = ConcurrentHashMap.newKeySet();

	public static void register(Object o) {
		if (o != null) {
			register(o.getClass(), o);
		}
	}

	public static void register(Class<?> c) {
		if (c != null) {
			register(c, null);
		}
	}

	private static void register(Class<?> c, Object o) {
		if (registered.contains(c)) {
			return;
		}

		String methodNamePrefix = c.getSimpleName();
		if (methodNamePrefix.endsWith("Impl")) {
			methodNamePrefix = methodNamePrefix.substring(0, methodNamePrefix.length() - 4);
		}
		methodNamePrefix += "_";

		Method[] methods = c.getMethods();
		for (Method m : methods) {
			String name = m.getName();
			Class<?>[] argClasses = m.getParameterTypes();
			if (argClasses.length > 0 && argClasses[0] == RuntimeContext.class && name.startsWith(methodNamePrefix)) {
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

	public static Method findMethod(ClassResolver classResolver, String name) {
		Method method = methods.get(name);
		if (method == null) {
			classResolver.processResolverException("native method '" + name + "' not found");
		}
		return method;
	}

	public static void invoke(ClassResolver classResolver, String name, Object[] args) {
		try {
			Method method = findMethod(classResolver, name);
			if (method != null) {
				Object o = objects.get(name);
				method.invoke(o, args);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			classResolver.processResolverException("native method '" + name + "' invocation error: " + exc);
		}
	}
}
