package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassJava;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.ool.model.nodes.NodeString;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HiMethodJava extends HiMethod {
	public Method method;

	public HiMethodJava(HiClassJava clazz, Method method, String name) {
		super(clazz, null, null, name, (NodeArgument[]) null, null, null);
		this.method = method;

		Class[] argJavaClasses = method.getParameterTypes();
		argCount = argJavaClasses.length;
		arguments = new NodeArgument[argCount];
		argNames = new String[argCount];
		for (int i = 0; i < argCount; i++) {
			Class argJavaClass = argJavaClasses[i];
			Type argType = null;
			if (argJavaClass.isPrimitive()) {
				argType = Type.getPrimitiveType(argJavaClass.getName());
			} else if (argJavaClass == String.class) {
				argType = Type.getTopType("String");
			}
			String argName = "arg" + i;
			arguments[i] = new NodeArgument(argType, argName, new Modifiers());
			argNames[i] = argName;
		}
	}

	@Override
	public void invoke(RuntimeContext ctx, HiClass type, Object object, HiField<?>[] arguments) {
		Object javaObject = ((HiObject) object).userObject;

		Object[] javaArgs = new Object[arguments.length - 1];
		for (int i = 0; i < arguments.length - 1; i++) { // ignore enclosing object
			HiField<?> argument = arguments[i];
			Object argValue = argument.getJava(ctx);
			if (argValue == null && !arguments[i].type.isNull()) {
				ctx.throwRuntimeException("Inconvertible java class argument: " + arguments[i].type.fullName);
				return;
			}
			javaArgs[i] = argValue;
		}

		try {
			Object resultJavaValue = method.invoke(javaObject, javaArgs);
			Object resultValue = convertFromJava(ctx, resultJavaValue);

			ctx.value.valueType = Value.VALUE;
			ctx.value.type = type;
			ctx.value.set(resultValue);
		} catch (Exception e) {
			ctx.throwRuntimeException(e.toString());
		}
	}

	public static Object convertFromJava(RuntimeContext ctx, Object javaObject) {
		if (javaObject == null) {
			return null;
		} else if (javaObject instanceof Number || javaObject instanceof Boolean || javaObject instanceof Character) {
			return javaObject;
		} else if (javaObject instanceof String) {
			return NodeString.createString(ctx, ((String) javaObject).toCharArray());
		} else if (javaObject instanceof Map) {
			return convertMapFromJava(ctx, (Map) javaObject);
		} else if (javaObject instanceof List) {
			return convertListFromJava(ctx, (List) javaObject);
		} else if (javaObject.getClass().isArray()) {
			return convertArrayFromJava(ctx, javaObject);
		}
		ctx.throwRuntimeException("Inconvertible method return value: " + javaObject.getClass());
		return null;
	}

	private static HiObject convertMapFromJava(RuntimeContext ctx, Map<?, ?> javaMap) {
		HiClass type = HiClass.forName(ctx, "HashMap");
		HiObject object = type.getConstructor(ctx).newInstance(ctx, null, null);
		Map map = (Map) object.userObject;
		for (Map.Entry e : javaMap.entrySet()) {
			map.put(convertFromJava(ctx, e.getKey()), convertFromJava(ctx, e.getValue()));
		}
		return object;
	}

	private static HiObject convertListFromJava(RuntimeContext ctx, List javaList) {
		HiClass type = HiClass.forName(ctx, "ArrayList");
		HiObject object = type.getConstructor(ctx).newInstance(ctx, null, null);
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
		if (rootElementClass.isPrimitive() || rootElementClass == String.class || HiObject.class.isAssignableFrom(rootElementClass)) {
			return javaArray;
		}
		int length = Array.getLength(javaArray);
		if (Map.class.isAssignableFrom(rootElementClass)) {
			className += "L" + HashMap.class.getName() + ";";
		} else if (List.class.isAssignableFrom(rootElementClass)) {
			className += "L" + ArrayList.class.getName() + ";";
		} else if (rootElementClass == Boolean.class) {
			className += "Z";
		} else if (rootElementClass == Byte.class) {
			className += "B";
		} else if (rootElementClass == Character.class) {
			className += "C";
		} else if (rootElementClass == Double.class) {
			className += "D";
		} else if (rootElementClass == Float.class) {
			className += "F";
		} else if (rootElementClass == Integer.class) {
			className += "I";
		} else if (rootElementClass == Long.class) {
			className += "J";
		} else if (rootElementClass == Short.class) {
			className += "S";
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

	@Override
	public boolean isJava() {
		return true;
	}
}
