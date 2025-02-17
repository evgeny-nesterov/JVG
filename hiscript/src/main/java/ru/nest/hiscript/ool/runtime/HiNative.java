package ru.nest.hiscript.ool.runtime;

import ru.nest.hiscript.ool.model.ClassResolver;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HiNative {
	private HiClassLoader classLoader;

	public final Map<String, Method> methods = new ConcurrentHashMap<>();

	public final Map<String, Object> objects = new ConcurrentHashMap<>();

	public final Set<Class<?>> registered = ConcurrentHashMap.newKeySet();

	public void register(Class<?> c) {
		register(c, null);
	}

	public void registerObject(Object o) {
		register(o.getClass(), o);
	}

	public HiNative(HiClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void registerObjects(List<Object> objects) {
		for (Object o : objects) {
			register(o.getClass(), o);
		}
	}

	private void register(Class<?> c, Object o) {
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
				this.methods.put(name, method);
				if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
					objects.put(name, c);
				} else {
					objects.put(name, o);
				}
			}
		}

		registered.add(c);
	}

	public Method findMethod(ClassResolver classResolver, String name) {
		Method method = HiClassLoader.getSystemClassLoader().getNative()._findMethod(classResolver, name);
		if (method != null) {
			return method;
		}
		HiClassLoader classLoader = this.classLoader;
		while (classLoader != null) {
			if (classLoader.getNative() != null) {
				method = classLoader.getNative()._findMethod(classResolver, name);
				if (method != null) {
					return method;
				}
			}
			classLoader = classLoader.getParent();
		}
		classResolver.processResolverException("native method '" + name + "' not found");
		return null;
	}

	private Method _findMethod(ClassResolver classResolver, String name) {
		Method method = methods.get(name);
		if (method == null) {
			String className = name.substring(0, name.indexOf('_'));
			HiClass.forName(new RuntimeContext(classResolver.getCompiler(), false), className);
			method = methods.get(name);
		}
		return method;
	}

	public void invoke(ClassResolver classResolver, Method method, Object[] args) {
		try {
			Object o = getObject(method.getName());
			if (o != null) {
				method.invoke(o, args);
			} else {
				classResolver.processResolverException("object not found by name: '" + method.getName() + "'");
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			classResolver.processResolverException("native method '" + method.getName() + "' invocation error: " + exc);
		}
	}

	private Object getObject(String name) {
		Object object = HiClassLoader.getSystemClassLoader().getNative().objects.get(name);
		if (object != null) {
			return object;
		}
		HiClassLoader classLoader = this.classLoader;
		while (classLoader != null) {
			if (classLoader.getNative() != null) {
				object = classLoader.getNative().objects.get(name);
				if (object != null) {
					return object;
				}
			}
			classLoader = classLoader.getParent();
		}
		return null;
	}
}
