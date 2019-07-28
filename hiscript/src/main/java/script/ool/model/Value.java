package script.ool.model;

import java.lang.reflect.Array;

import script.ool.model.classes.ClazzArray;
import script.ool.model.fields.FieldPrimitive;
import script.ool.model.lib.ImplUtil;

/**
 * ������ ������ ������. �������� ���������� ���� ����� ������.
 */
public class Value implements PrimitiveTypes {
	private final static char[] NULL = "null".toCharArray();

	public final static int VALUE = 0;

	public final static int TYPE = 1;

	public final static int VARIABLE = 2;

	public final static int CLASS = 3;

	public final static int METHOD = 4;

	public final static int ARRAY_INDEX = 5;

	public final static int NAME = 6;

	public final static int EXECUTE = 7; // for node

	private RuntimeContext ctx;

	public Value(RuntimeContext ctx) {
		this.ctx = ctx;
	}

	public Node node;

	public int valueType;

	public Clazz type;

	// ���������� �� ����� ���������� ������ ������
	public byte byteNumber;

	public short shortNumber;

	public int intNumber;

	public float floatNumber;

	public long longNumber;

	public double doubleNumber;

	public char character;

	public boolean bool;

	public Obj object;

	public Object array; // Obj[]...[] or <primitive>[]...[]

	public Object parentArray;

	public int arrayIndex;

	// Used in expressions as
	// field name, method name, class name or package name.
	// This is not a value, this is an intermediate condition.
	public String name;

	public Type variableType;

	public Field<?> variable;

	// Method arguments
	public Node[] arguments;

	public void clear() {
		type = null;
		object = null;
		array = null;
		parentArray = null;
		name = null;
		variableType = null;
		variable = null;
		arguments = null;
		node = null;
	}

	/**
	 * ���������� ������� �������� � ���� ������� � ����������� �� ����.
	 */
	public Object get() {
		// array
		if (type.isArray()) {
			return array;
		}

		// primitives
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BOOLEAN:
					return bool;

				case CHAR:
					return character;

				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case INT:
					return intNumber;

				case LONG:
					return longNumber;

				case FLOAT:
					return floatNumber;

				case DOUBLE:
					return doubleNumber;
			}
		}

		return object;
	}

	public void set(Object value) {
		if (value instanceof Obj || value == null) {
			valueType = VALUE;
			if (value != null) {
				object = (Obj) value;
				type = object.clazz;
			} else {
				object = null;
				type = Clazz.OBJECT_CLASS;
			}
		} else {
			// arrays
			Class<? extends Object> clazz = value.getClass();
			if (clazz.isArray()) {
				valueType = VALUE;
				type = Clazz.getArrayType(clazz);
				array = value;
			}
			// primitives
			else if (value instanceof Double) {
				valueType = VALUE;
				doubleNumber = (Double) value;
				type = Clazz.getPrimitiveClass("double");
			} else if (value instanceof Float) {
				valueType = VALUE;
				floatNumber = (Float) value;
				type = Clazz.getPrimitiveClass("float");
			} else if (value instanceof Long) {
				valueType = VALUE;
				longNumber = (Long) value;
				type = Clazz.getPrimitiveClass("long");
			} else if (value instanceof Integer) {
				valueType = VALUE;
				intNumber = (Integer) value;
				type = Clazz.getPrimitiveClass("int");
			} else if (value instanceof Short) {
				valueType = VALUE;
				shortNumber = (Short) value;
				type = Clazz.getPrimitiveClass("short");
			} else if (value instanceof Byte) {
				valueType = VALUE;
				byteNumber = (Byte) value;
				type = Clazz.getPrimitiveClass("byte");
			} else if (value instanceof Character) {
				valueType = VALUE;
				character = (Character) value;
				type = Clazz.getPrimitiveClass("char");
			} else if (value instanceof Boolean) {
				valueType = VALUE;
				bool = (Boolean) value;
				type = Clazz.getPrimitiveClass("boolean");
			}
		}
	}

	public Obj getObject() {
		if (type.isPrimitive() || type.isArray()) {
			ctx.throwException("object is expected");
			return null;
		}

		return object;
	}

	public Object getArray() {
		if (!type.isArray()) {
			ctx.throwException("array is expected: " + type.fullName);
			return null;
		}

		return array;
	}

	public boolean getBoolean() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			if (typeIndex == BOOLEAN) {
				return bool;
			}
		}

		ctx.throwException("boolean is expected");
		return false;
	}

	public char getChar() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			if (typeIndex == CHAR) {
				return character;
			}
		}

		ctx.throwException("char is expected");
		return 0;
	}

	public byte getByte() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			if (typeIndex == BYTE) {
				return byteNumber;
			}
		}

		ctx.throwException("byte is expected");
		return 0;
	}

	public short getShort() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;
			}
		}

		ctx.throwException("short is expected");
		return 0;
	}

	public int getInt() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case CHAR:
					return character;

				case INT:
					return intNumber;
			}
		}

		ctx.throwException("int is expected");
		return 0;
	}

	public long getLong() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case CHAR:
					return character;

				case INT:
					return intNumber;

				case LONG:
					return longNumber;
			}
		}

		ctx.throwException("long is expected");
		return 0;
	}

	public float getFloat() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case CHAR:
					return character;

				case INT:
					return intNumber;

				case LONG:
					return longNumber;

				case FLOAT:
					return floatNumber;
			}
		}

		ctx.throwException("float is expected");
		return 0;
	}

	public double getDouble() {
		if (type.isPrimitive()) {
			int typeIndex = FieldPrimitive.getType(type);
			switch (typeIndex) {
				case BYTE:
					return byteNumber;

				case SHORT:
					return shortNumber;

				case CHAR:
					return character;

				case INT:
					return intNumber;

				case LONG:
					return longNumber;

				case FLOAT:
					return floatNumber;

				case DOUBLE:
					return doubleNumber;
			}
		}

		ctx.throwException("double is expected");
		return 0;
	}

	// TODO: optimize
	public void copyTo(Value dst) {
		dst.valueType = valueType;
		dst.type = type;
		dst.name = name;

		// VALUE
		dst.array = array;
		dst.bool = bool;
		dst.byteNumber = byteNumber;
		dst.character = character;
		dst.doubleNumber = doubleNumber;
		dst.floatNumber = floatNumber;
		dst.intNumber = intNumber;
		dst.longNumber = longNumber;
		dst.object = object;
		dst.shortNumber = shortNumber;

		// VARIABLE
		dst.variable = variable;

		// TYPE
		dst.variableType = variableType;

		// METHOD
		dst.arguments = arguments;

		// ARRAY_INDEX
		dst.parentArray = parentArray;
		dst.arrayIndex = arrayIndex;

		// EXECUTE
		dst.node = node;
	}

	public void copyToArray(Value value) {
		// TODO: copy array and object

		int typeIndex = FieldPrimitive.getType(type);
		switch (typeIndex) {
			case BOOLEAN:
				Array.setBoolean(parentArray, arrayIndex, value.bool);
				break;

			case CHAR:
				Array.setChar(parentArray, arrayIndex, value.character);
				break;

			case BYTE:
				Array.setByte(parentArray, arrayIndex, value.byteNumber);
				break;

			case SHORT:
				Array.setShort(parentArray, arrayIndex, value.shortNumber);
				break;

			case INT:
				Array.setInt(parentArray, arrayIndex, value.intNumber);
				break;

			case LONG:
				Array.setLong(parentArray, arrayIndex, value.longNumber);
				break;

			case FLOAT:
				Array.setFloat(parentArray, arrayIndex, value.floatNumber);
				break;

			case DOUBLE:
				Array.setDouble(parentArray, arrayIndex, value.doubleNumber);
				break;
		}
	}

	public char[] getString(RuntimeContext ctx) {
		if (type.isPrimitive()) {
			int t = FieldPrimitive.getType(type);
			switch (t) {
				case BOOLEAN:
					return Boolean.toString(bool).toCharArray();

				case CHAR:
					return Character.toString(character).toCharArray();

				case BYTE:
					return Byte.toString(byteNumber).toCharArray();

				case SHORT:
					return Short.toString(shortNumber).toCharArray();

				case INT:
					return Integer.toString(intNumber).toCharArray();

				case LONG:
					return Long.toString(longNumber).toCharArray();

				case FLOAT:
					return Float.toString(floatNumber).toCharArray();

				case DOUBLE:
					return Double.toString(doubleNumber).toCharArray();
			}
		}

		if (type.isArray()) {
			if (array == null) {
				return NULL;
			} else {
				ClazzArray arrayType = (ClazzArray) type;
				return (arrayType.className + "@" + Integer.toHexString(array.hashCode())).toCharArray();
			}
		}

		if (type.isNull() || object == null) {
			return NULL;
		}

		boolean isString = object.clazz.fullName != null && "String".equals(object.clazz.fullName);
		if (isString) {
			return ImplUtil.getChars(object);
		}

		if (object == null) {
			return NULL;
		} else {
			char[] toString = object.toString(ctx);
			if (toString != null) {
				return toString;
			} else {
				return NULL;
			}
		}
	}

	@Override
	public String toString() {
		switch (valueType) {
			case VALUE:
				return "VALUE: type=" + type + ", value=" + get();
			case TYPE:
				return "TYPE: variableType=" + variableType;
			case VARIABLE:
				return "VARIABLE: name=" + name + ", variable=" + variable;
			case CLASS:
				return "CLASS: type=" + type + ", name=" + name;
			case METHOD:
				return "METHOD: name=" + name + ", arguments=" + arguments;
			case ARRAY_INDEX:
				return "ARRAY INDEX: type=" + type + ", parent array=" + parentArray + ", ara index=" + arrayIndex;
			case NAME:
				return "NAME: name=" + name;
			case EXECUTE:
				return "EXECUTE: node=" + node;
		}
		return "";
	}
}
