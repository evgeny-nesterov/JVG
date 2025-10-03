package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class HiJava {
	public static Object convertFromJava(RuntimeContext ctx, Object javaObject) {
		// @autoboxing
		if (javaObject == null) {
			return null;
		} else if (javaObject instanceof Integer) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.INT.box(ctx, ctx.value);
		} else if (javaObject instanceof Boolean) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.BOOLEAN.box(ctx, ctx.value);
		} else if (javaObject instanceof Long) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.LONG.box(ctx, ctx.value);
		} else if (javaObject instanceof Double) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.DOUBLE.box(ctx, ctx.value);
		} else if (javaObject instanceof Character) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.CHAR.box(ctx, ctx.value);
		} else if (javaObject instanceof Byte) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.BYTE.box(ctx, ctx.value);
		} else if (javaObject instanceof Short) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.SHORT.box(ctx, ctx.value);
		} else if (javaObject instanceof Float) {
			ctx.setValue(javaObject);
			return HiClassPrimitive.FLOAT.box(ctx, ctx.value);
		} else if (javaObject instanceof String) {
			return NodeString.createString(ctx, (String) javaObject, false);
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
		HiClass clazz = HiClass.forName(ctx, HiClass.HASHMAP_CLASS_NAME);
		HiObject object = clazz.getConstructor(ctx).newInstance(ctx, Type.getType(clazz), null, null);
		if (object != null) {
			Map map = (Map) object.userObject;
			for (Map.Entry e : javaMap.entrySet()) {
				Object key = convertFromJava(ctx, e.getKey());
				Object value = convertFromJava(ctx, e.getValue());
				map.put(key, value);
			}
		}
		return object;
	}

	private static HiObject convertListFromJava(RuntimeContext ctx, List javaList) {
		HiClass clazz = HiClass.forName(ctx, HiClass.ARRAYLIST_CLASS_NAME);
		HiObject object = clazz.getConstructor(ctx).newInstance(ctx, null, null, null);
		if (object != null) {
			List list = (List) object.userObject;
			for (Object o : javaList) {
				list.add(convertFromJava(ctx, o));
			}
		}
		return object;
	}

	/**
	 * @return array of java objects or Value with array value and array class
	 */
	private static Object convertArrayFromJava(RuntimeContext ctx, Object javaArray) {
		Class javaArrayClass = javaArray.getClass();
		Class rootElementClass = javaArrayClass.getComponentType();
		String className = "[";
		while (rootElementClass.isArray()) {
			rootElementClass = rootElementClass.getComponentType();
			className += "[";
		}
		if (rootElementClass.isPrimitive() || rootElementClass == String.class || rootElementClass == int.class || rootElementClass == long.class || rootElementClass == double.class || rootElementClass == char.class || rootElementClass == byte.class || rootElementClass == float.class || rootElementClass == short.class || HiObject.class.isAssignableFrom(rootElementClass)) {
			return javaArray;
		}
		try {
			int length = Array.getLength(javaArray);
			className += "L" + HiObject.class.getName() + ";";
			Class elementJavaClass = Class.forName(className);
			Object array = Array.newInstance(elementJavaClass.getComponentType(), length);
			for (int i = 0; i < length; i++) {
				Object elementValue = convertFromJava(ctx, Array.get(javaArray, i));
				Array.set(array, i, elementValue);
			}
			HiClass arrayClass = HiClassArray.getArrayElementClass(javaArrayClass);
			Value value = new Value(ctx);
			value.setArrayValue(arrayClass, array);
			return value;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	public static Type getTypeByJavaClass(Class javaClass, HiRuntimeEnvironment env) {
		String javaClassName = javaClass.getSimpleName();
		Type primitiveType = Type.getPrimitiveType(javaClassName);
		if (primitiveType != null) {
			return primitiveType;
		}

		if (javaClass.isArray()) {
			// TODO packages
		} else {
			return Type.getTopType(javaClassName, env);
		}

		if (Map.class.isAssignableFrom(javaClass)) {
			return Type.getTopType(HiClass.HASHMAP_CLASS_NAME, env);
		} else if (List.class.isAssignableFrom(javaClass)) {
			return Type.getTopType(HiClass.ARRAYLIST_CLASS_NAME, env);
		} else if (javaClass.isArray()) {
			int dimension = 1;
			Class elementClass = javaClass.getComponentType();
			while (elementClass.isArray()) {
				elementClass = elementClass.getComponentType();
				dimension++;
			}
			Type rootElementType = getTypeByJavaClass(elementClass, env);
			if (rootElementType != null) {
				return Type.getArrayType(rootElementType, dimension, env);
			}
		}
		return Type.getTopType(javaClassName, env);
	}
}
