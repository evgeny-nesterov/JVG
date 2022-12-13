package ru.nest.hiscript.ool.model;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class HiFieldJava extends HiField {
	private Field field;

	public HiFieldJava(Field field, String name) {
		super(null, name);
		this.field = field;
		field.setAccessible(true);
		type = getTypeByJavaClass(field.getType());
		declared = true;
		initialized = true;
	}

	public static Type getTypeByJavaClass(Class javaClass) {
		if (javaClass == Integer.class || javaClass == int.class) {
			return Type.getPrimitiveType("int");
		} else if (javaClass == Long.class || javaClass == long.class) {
			return Type.getPrimitiveType("long");
		} else if (javaClass == Double.class || javaClass == double.class) {
			return Type.getPrimitiveType("double");
		} else if (javaClass == Boolean.class || javaClass == boolean.class) {
			return Type.getPrimitiveType("boolean");
		} else if (javaClass == Byte.class || javaClass == byte.class) {
			return Type.getPrimitiveType("byte");
		} else if (javaClass == Float.class || javaClass == float.class) {
			return Type.getPrimitiveType("float");
		} else if (javaClass == Short.class || javaClass == short.class) {
			return Type.getPrimitiveType("short");
		} else if (javaClass == Character.class || javaClass == char.class) {
			return Type.getPrimitiveType("char");
		} else if (javaClass == String.class) {
			return Type.getTopType("String");
		} else if (Map.class.isAssignableFrom(javaClass)) {
			return Type.getTopType("HashMap");
		} else if (List.class.isAssignableFrom(javaClass)) {
			return Type.getTopType("ArrayList");
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
		return null;
	}

	@Override
	public boolean isStatic() {
		return java.lang.reflect.Modifier.isStatic(field.getModifiers());
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		if (value.object.userObject == null) {
			ctx.throwRuntimeException("Null pointer");
			return;
		}

		try {
			Object resultJavaValue = field.get(value.object.userObject);
			Object resultValue = HiMethodJava.convertFromJava(ctx, resultJavaValue);
			value.set(resultValue);
		} catch (IllegalAccessException e) {
			ctx.throwRuntimeException(e.toString());
		}
	}

	@Override
	public Object get() {
		return null;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return null;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {

	}
}
