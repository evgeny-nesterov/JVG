package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeString;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiJava {
	public static Object convertFromJava(RuntimeContext ctx, Object javaObject) {
		if (javaObject == null) {
			return null;
		} else if (javaObject instanceof Number || javaObject instanceof Boolean || javaObject instanceof Character) {
			return javaObject;
		} else if (javaObject instanceof String) {
			return NodeString.createString(ctx, (String) javaObject);
		} else if (javaObject instanceof Map) {
			return convertMapFromJava(ctx, (Map) javaObject);
		} else if (javaObject instanceof List) {
			return convertListFromJava(ctx, (List) javaObject);
		} else if (javaObject.getClass().isArray()) {
			return convertArrayFromJava(ctx, javaObject);
		}
		ctx.throwRuntimeException("inconvertible method return value: " + javaObject.getClass());
		return null;
	}

	private static HiObject convertMapFromJava(RuntimeContext ctx, Map<?, ?> javaMap) {
		HiClass type = HiClass.forName(ctx, HiClass.HASHMAP_CLASS_NAME);
		HiObject object = type.getConstructor(ctx).newInstance(ctx, Type.getType(type), null, null);
		Map map = (Map) object.userObject;
		for (Map.Entry e : javaMap.entrySet()) {
			map.put(convertFromJava(ctx, e.getKey()), convertFromJava(ctx, e.getValue()));
		}
		return object;
	}

	private static HiObject convertListFromJava(RuntimeContext ctx, List javaList) {
		HiClass type = HiClass.forName(ctx, HiClass.ARRAYLIST_CLASS_NAME);
		HiObject object = type.getConstructor(ctx).newInstance(ctx, null, null, null);
		List list = (List) object.userObject;
		for (Object o : javaList) {
			list.add(convertFromJava(ctx, o));
		}
		return object;
	}

	private static Object convertArrayFromJava(RuntimeContext ctx, Object javaArray) {
		Class javaArrayClass = javaArray.getClass();
		Class rootElementClass = javaArrayClass.getComponentType();
		String className = "[";
		while (rootElementClass.isArray()) {
			rootElementClass = rootElementClass.getComponentType();
			className += "[";
		}
		if (rootElementClass.isPrimitive() || rootElementClass == String.class || Number.class.isAssignableFrom(rootElementClass) || rootElementClass == Boolean.class || rootElementClass == Character.class || HiObject.class.isAssignableFrom(rootElementClass)) {
			return javaArray;
		}
		int length = Array.getLength(javaArray);
		if (Map.class.isAssignableFrom(rootElementClass)) {
			className += "L" + HashMap.class.getName() + ";";
		} else if (List.class.isAssignableFrom(rootElementClass)) {
			className += "L" + ArrayList.class.getName() + ";";
		} else {
			className += "L" + Object.class.getName() + ";";
		}
		try {
			Class elementClass = Class.forName(className);
			Object array = Array.newInstance(elementClass, length);
			for (int i = 0; i < length; i++) {
				Object elementValue = convertFromJava(ctx, Array.get(javaArray, i));
				if (elementClass.isArray() || HiObject.class.isAssignableFrom(elementClass)) {
					Array.set(array, i, elementValue);
				} else if (elementClass == Integer.class) {
					Array.setInt(array, i, (Integer) elementValue);
				} else if (elementClass == Long.class) {
					Array.setLong(array, i, (Long) elementValue);
				} else if (elementClass == Double.class) {
					Array.setDouble(array, i, (Double) elementValue);
				} else if (elementClass == Boolean.class) {
					Array.setBoolean(array, i, (Boolean) elementValue);
				} else if (elementClass == Character.class) {
					Array.setChar(array, i, (Character) elementValue);
				} else if (elementClass == Byte.class) {
					Array.setByte(array, i, (Byte) elementValue);
				} else if (elementClass == Float.class) {
					Array.setFloat(array, i, (Float) elementValue);
				} else if (elementClass == Short.class) {
					Array.setShort(array, i, (Short) elementValue);
				}
			}
			return array;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Type getTypeByJavaClass(Class javaClass) {
		Type primitiveType = Type.getPrimitiveType(javaClass.getSimpleName());
		if (primitiveType != null) {
			return primitiveType;
		}

		if (javaClass == String.class) {
			return Type.getTopType(HiClass.STRING_CLASS_NAME);
		} else if (Number.class.isAssignableFrom(javaClass)) {
			return Type.getTopType(HiClass.HASHMAP_CLASS_NAME);
		} else if (javaClass == Boolean.class || javaClass == Character.class) {
			return Type.getTopType(HiClass.HASHMAP_CLASS_NAME);
		} else if (Map.class.isAssignableFrom(javaClass)) {
			return Type.getTopType(HiClass.HASHMAP_CLASS_NAME);
		} else if (List.class.isAssignableFrom(javaClass)) {
			return Type.getTopType(HiClass.ARRAYLIST_CLASS_NAME);
		} else if (javaClass.isArray()) {
			int dimension = 1;
			Class elementClass = javaClass.getComponentType();
			while (elementClass.isArray()) {
				elementClass = elementClass.getComponentType();
				dimension++;
			}
			Type rootElementType = getTypeByJavaClass(elementClass);
			if (rootElementType != null) {
				return Type.getArrayType(rootElementType, dimension);
			}
		}
		return Type.getTopType(javaClass.getSimpleName());
	}
}
