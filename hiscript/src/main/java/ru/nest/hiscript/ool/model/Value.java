package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldPrimitive;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

import java.lang.reflect.Array;

/**
 * Буфер обмена данных. Содержит переменные всех типов данных.
 */
public class Value implements PrimitiveTypes {
	private final static char[] NULL = "null".toCharArray();

	public final static int VALUE = 0;

	public final static int TYPE = 1;

	public final static int VARIABLE = 2;

	public final static int CLASS = 3;

	public final static int METHOD_INVOCATION = 4;

	public final static int ARRAY_INDEX = 5;

	public final static int NAME = 6;

	public final static int EXECUTE = 7; // for node

	public RuntimeContext ctx;

	public Value(RuntimeContext ctx) {
		this.ctx = ctx;
	}

	public HiNodeIF node;

	public int valueType;

	public HiClass type;

	public HiClass lambdaClass;

	// Переменные со всеми возможными типами данных
	public byte byteNumber;

	public short shortNumber;

	public int intNumber;

	public float floatNumber;

	public long longNumber;

	public double doubleNumber;

	public char character;

	public boolean bool;

	public HiObject object;

	public Object array; // Obj[]...[] or <primitive>[]...[]

	public Object parentArray;

	public int arrayIndex;

	// Used in expressions as
	// field name, method name, class name or package name.
	// This is not a value, this is an intermediate condition.
	public String name;

	public int nameDimensions;

	public String castedVariableName;

	public HiNode castedCondition;

	public NodeArgument[] castedRecordArguments;

	public Type variableType;

	public HiField<?> variable;

	// Method arguments
	public HiNode[] arguments;

	public void clear() {
		node = null;
		type = null;
		lambdaClass = null;
		object = null;
		array = null;
		parentArray = null;
		name = null;
		nameDimensions = 0;
		castedVariableName = null;
		castedCondition = null;
		castedRecordArguments = null;
		variableType = null;
		variable = null;
		arguments = null;
	}

	/**
	 * Возвращает текущее значение в виде объекта в зависимости от типа.
	 */
	public Object get() {
		if (valueType == VARIABLE) {
			return variable.get();
		}

		// array
		if (type.isArray()) {
			return array;
		}

		// primitives
		if (type.isPrimitive()) {
			int typeIndex = HiFieldPrimitive.getType(type);
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

	public boolean set(Object value) {
		lambdaClass = null;
		if (value instanceof HiObject || value == null) {
			valueType = VALUE;
			if (value != null) {
				object = (HiObject) value;
				type = object.clazz;
				return true;
			} else {
				object = null;
				type = HiClass.OBJECT_CLASS;
				return true;
			}
		} else {
			// arrays
			Class<? extends Object> clazz = value.getClass();
			if (clazz.isArray()) {
				valueType = VALUE;
				type = HiClass.getArrayType(clazz);
				array = value;
				return true;
			}
			// primitives
			else if (value instanceof Double) {
				valueType = VALUE;
				doubleNumber = (Double) value;
				type = HiClassPrimitive.DOUBLE;
				return true;
			} else if (value instanceof Float) {
				valueType = VALUE;
				floatNumber = (Float) value;
				type = HiClassPrimitive.FLOAT;
				return true;
			} else if (value instanceof Long) {
				valueType = VALUE;
				longNumber = (Long) value;
				type = HiClassPrimitive.LONG;
				return true;
			} else if (value instanceof Integer) {
				valueType = VALUE;
				intNumber = (Integer) value;
				type = HiClassPrimitive.INT;
				return true;
			} else if (value instanceof Short) {
				valueType = VALUE;
				shortNumber = (Short) value;
				type = HiClassPrimitive.SHORT;
				return true;
			} else if (value instanceof Byte) {
				valueType = VALUE;
				byteNumber = (Byte) value;
				type = HiClassPrimitive.BYTE;
				return true;
			} else if (value instanceof Character) {
				valueType = VALUE;
				character = (Character) value;
				type = HiClassPrimitive.CHAR;
				return true;
			} else if (value instanceof Boolean) {
				valueType = VALUE;
				bool = (Boolean) value;
				type = HiClassPrimitive.BOOLEAN;
				return true;
			}
			return false;
		}
	}

	public HiObject getObject() {
		if (type.isPrimitive() || type.isArray()) {
			ctx.throwRuntimeException("object is expected");
			return null;
		}
		return object;
	}

	// autobox
	public HiObject getObject(HiClass dstClass) {
		if (type.isArray()) {
			ctx.throwRuntimeException("object is expected");
			return null;
		}
		if (type.isPrimitive()) {
			HiClassPrimitive primitiveClass = dstClass != null && dstClass.getAutoboxedPrimitiveClass() != null ? dstClass.getAutoboxedPrimitiveClass() : (HiClassPrimitive) type;
			return primitiveClass.autobox(ctx, this); // changes ctx
		} else {
			return object;
		}
	}

	public String getStringValue(RuntimeContext ctx) {
		HiObject object = getObject();
		if (object != null) {
			return object.getStringValue(ctx);
		}
		return null;
	}

	public Object getArray() {
		if (!type.isArray() && !type.isNull()) {
			ctx.throwRuntimeException("array is expected: " + type.fullName);
			return null;
		}
		return array;
	}

	// autobox
	public boolean getAutoboxPrimitiveValue(int expectedTypeIndex) {
		if (type.isPrimitive()) {
			int typeIndex = HiFieldPrimitive.getType(type);
			if (typeIndex == expectedTypeIndex) {
				return true;
			}
		} else if (type.getAutoboxedPrimitiveClass() != null) {
			int typeIndex = HiFieldPrimitive.getType(type.getAutoboxedPrimitiveClass());
			if (typeIndex == expectedTypeIndex) {
				if (object != null) {
					object.getAutoboxedValue(ctx, this);
					int autoboxTypeIndex = HiFieldPrimitive.getType(type);
					if (autoboxTypeIndex == expectedTypeIndex) {
						return true;
					}
				} else {
					ctx.throwRuntimeException("null pointer");
					return false;
				}
			}
		}
		return false;
	}

	// autobox
	public int getAutoboxValues(int... expectedTypesIndexes) {
		if (type.isPrimitive()) {
			int typeIndex = HiFieldPrimitive.getType(type);
			for (int i = 0; i < expectedTypesIndexes.length; i++) {
				int expectedTypeIndex = expectedTypesIndexes[i];
				if (typeIndex == expectedTypeIndex) {
					return typeIndex;
				}
			}
		} else if (type.getAutoboxedPrimitiveClass() != null) {
			int typeIndex = HiFieldPrimitive.getType(type.getAutoboxedPrimitiveClass());
			for (int i = 0; i < expectedTypesIndexes.length; i++) {
				int expectedTypeIndex = expectedTypesIndexes[i];
				if (typeIndex == expectedTypeIndex) {
					if (object != null) {
						object.getAutoboxedValue(ctx, this);
						int autoboxTypeIndex = HiFieldPrimitive.getType(type);
						if (autoboxTypeIndex == expectedTypeIndex) {
							return autoboxTypeIndex;
						}
					} else {
						ctx.throwRuntimeException("null pointer");
					}
					return -1;
				}
			}
		}
		return -1;
	}

	// autobox
	public void unbox() {
		if (type.getAutoboxedPrimitiveClass() != null) {
			if (object != null) {
				substitutePrimitiveValueFromAutoboxValue();
				type = type.getAutoboxedPrimitiveClass();
			} else {
				ctx.throwRuntimeException("null pointer");
			}
		}
	}

	// autobox
	public void substitutePrimitiveValueFromAutoboxValue() {
		int t = HiFieldPrimitive.getType(type.autoboxedPrimitiveClass);
		if (object == null) {
			ctx.throwRuntimeException("null pointer");
			return;
		}
		HiField valueField = object.getField(ctx, "value");
		switch (t) {
			case BOOLEAN:
				bool = (Boolean) valueField.get();
				break;
			case CHAR:
				Object valueObject = valueField.get();
				if (valueObject instanceof Character) {
					character = (Character) valueObject;
				} else {
					character = (char) ((Number) valueField.get()).intValue();
				}
				break;
			case BYTE:
				byteNumber = ((Number) valueField.get()).byteValue();
				break;
			case SHORT:
				shortNumber = ((Number) valueField.get()).shortValue();
				break;
			case INT:
				intNumber = ((Number) valueField.get()).intValue();
				break;
			case LONG:
				longNumber = ((Number) valueField.get()).longValue();
				break;
			case FLOAT:
				floatNumber = ((Number) valueField.get()).floatValue();
				break;
			case DOUBLE:
				doubleNumber = ((Number) valueField.get()).doubleValue();
				break;
		}
	}

	public boolean getBoolean() {
		if (getAutoboxPrimitiveValue(BOOLEAN)) {
			return bool;
		}
		ctx.throwRuntimeException("boolean is expected");
		return false;
	}

	public char getChar() {
		if (getAutoboxPrimitiveValue(CHAR)) {
			return character;
		}
		ctx.throwRuntimeException("char is expected");
		return 0;
	}

	public byte getByte() {
		if (getAutoboxPrimitiveValue(BYTE)) {
			return byteNumber;
		}
		ctx.throwRuntimeException("byte is expected");
		return 0;
	}

	public short getShort() {
		int typeIndex = getAutoboxValues(BYTE, SHORT);
		if (typeIndex != -1) {
			switch (typeIndex) {
				case BYTE:
					return byteNumber;
				case SHORT:
					return shortNumber;
			}
		}
		ctx.throwRuntimeException("short is expected");
		return 0;
	}

	public int getInt() {
		int typeIndex = getAutoboxValues(BYTE, SHORT, CHAR, INT);
		if (typeIndex != -1) {
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
		ctx.throwRuntimeException("int is expected");
		return 0;
	}

	public long getLong() {
		int typeIndex = getAutoboxValues(BYTE, SHORT, CHAR, INT, LONG);
		if (typeIndex != -1) {
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
		ctx.throwRuntimeException("long is expected");
		return 0;
	}

	public float getFloat() {
		int typeIndex = getAutoboxValues(BYTE, SHORT, CHAR, INT, LONG, FLOAT);
		if (typeIndex != -1) {
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
		ctx.throwRuntimeException("float is expected");
		return 0;
	}

	public double getDouble() {
		int typeIndex = getAutoboxValues(BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE);
		if (typeIndex != -1) {
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
		ctx.throwRuntimeException("double is expected");
		return 0;
	}

	// TODO: optimize
	public void copyTo(Value dst) {
		if (dst == this) {
			return;
		}

		dst.valueType = valueType;
		dst.type = type;
		dst.lambdaClass = lambdaClass;
		dst.name = name;
		dst.nameDimensions = nameDimensions;

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

		// cast
		dst.castedVariableName = castedVariableName;
		dst.castedRecordArguments = castedRecordArguments;
		dst.castedCondition = castedCondition;
	}

	public void copyToArray(Value value) {
		// TODO: copy array and object

		int typeIndex = HiFieldPrimitive.getType(type);
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
			int t = HiFieldPrimitive.getType(type);
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
				HiClassArray arrayType = (HiClassArray) type;
				return (arrayType.className + "@" + Integer.toHexString(array.hashCode())).toCharArray();
			}
		}

		if (type.isNull() || object == null) {
			return NULL;
		}

		boolean isString = object.clazz.fullName != null && HiClass.STRING_CLASS_NAME.equals(object.clazz.fullName);
		if (isString) {
			return ImplUtil.getChars(ctx, object);
		}

		char[] toString = object.getStringChars(ctx);
		if (toString != null) {
			return toString;
		} else {
			return NULL;
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
			case METHOD_INVOCATION:
				return "METHOD: name=" + name + ", arguments=" + arguments;
			case ARRAY_INDEX:
				return "ARRAY INDEX: type=" + type + ", parent array=" + parentArray + ", ara index=" + arrayIndex;
			case NAME:
				return "NAME: name=" + name + (nameDimensions > 0 ? "[]" : "");
			case EXECUTE:
				return "EXECUTE: node=" + node;
		}
		return "";
	}

	public HiClass getOperationClass() {
		if (type.getAutoboxedPrimitiveClass() != null) {
			// autobox
			substitutePrimitiveValueFromAutoboxValue();
			return type.getAutoboxedPrimitiveClass();
		} else if (object != null && type.isObject()) {
			return object.clazz;
		}
		return type;
	}
}
