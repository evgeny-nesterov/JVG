package ru.nest.hiscript.ool.runtime;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.JavaString;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.java.HiConstructorJava;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HiRuntimeEnvironment {
	private final Map<String, Type> typesWithoutParent = new HashMap<>();

	private final Map<Type, Type> arrayTypes = new HashMap<>();

	public final Map<HiClassArray, Class> javaClassesMap = new ConcurrentHashMap<>();

	public final Map<Integer, HiConstructorJava> javaConstructorsMap = new ConcurrentHashMap<>();

	public Map<JavaString, HiObject> strings = new ConcurrentHashMap<>();

	public HiRuntimeEnvironment() {
		rootClassLoader = HiClassLoader.createRoot(this);
		userClassLoader = new HiClassLoader(HiClassLoader.USER_CLASS_LOADER_NAME, rootClassLoader, this);
		stringType = Type.getTopType(HiClass.STRING_CLASS_NAME, this);
	}

	public Type getTopType(String name) {
		Type type = typesWithoutParent.get(name);
		if (type == null) {
			type = new Type(null, name);
			typesWithoutParent.put(name, type);
		}
		return type;
	}

	public Type getArrayType(Type type) {
		Type arrayType = arrayTypes.get(type);
		if (arrayType == null) {
			arrayType = new Type(type);
			arrayTypes.put(type, arrayType);
		}
		return arrayType;
	}

	public Type stringType;

	public HiClassLoader rootClassLoader;

	public HiClassLoader userClassLoader;

	public void setUserClassLoader(HiClassLoader userClassLoader) {
		rootClassLoader.removeClassLoader(this.userClassLoader);
		rootClassLoader.addClassLoader(userClassLoader);
		this.userClassLoader = userClassLoader;
	}

	public HiClassLoader getRootClassLoader() {
		return rootClassLoader;
	}

	public HiClassLoader getUserClassLoader() {
		return userClassLoader;
	}

	public void clear() {
		userClassLoader.clear();
	}

}
