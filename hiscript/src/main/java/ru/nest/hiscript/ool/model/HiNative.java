package ru.nest.hiscript.ool.model;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiNative {
	private static final Map<String, Method> methods = new ConcurrentHashMap<>();

	private static final Map<String, Object> objects = new ConcurrentHashMap<>();

	private static final Set<Class<?>> registered = ConcurrentHashMap.newKeySet();

	public static void register(Class<?> c) {
		if (c != null) {
			register(c, null);
		}
	}

	public static void registerObject(Object o) {
		register(o.getClass(), o);
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
		for (Method method : methods) {
			String name = method.getName();
			Class<?>[] argClasses = method.getParameterTypes();
			if (argClasses.length > 0 && argClasses[0] == RuntimeContext.class && (name.startsWith(methodNamePrefix) || name.startsWith("root$"))) {
				method.setAccessible(true);
				HiNative.methods.put(name, method);
				if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
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
			String className = name.substring(0, name.indexOf('_'));
			HiClass.forName(new RuntimeContext(classResolver.getCompiler(), false), className);
			method = methods.get(name);
			if (method == null) {
				classResolver.processResolverException("native method '" + name + "' not found");
			}
		}
		return method;
	}

	public static void invoke(ClassResolver classResolver, Method method, Object[] args) {
		try {
			Object o = objects.get(method.getName());
			method.invoke(o, args);
		} catch (Exception exc) {
			exc.printStackTrace();
			classResolver.processResolverException("native method '" + method.getName() + "' invocation error: " + exc);
		}
	}
}
