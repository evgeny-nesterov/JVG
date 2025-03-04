package ru.nest.hiscript.ool.runtime;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.lib.ImplUtil;
import ru.nest.hiscript.ool.model.nodes.NodeString;

import java.lang.reflect.Array;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;
import static ru.nest.hiscript.ool.runtime.ValueType.*;

/**
 * Буфер обмена данных. Содержит переменные всех типов данных.
 */
public class Value {
	private final static char[] NULL_CHARS = "null".toCharArray();

	public RuntimeContext ctx;

	public Value(RuntimeContext ctx) {
		this.ctx = ctx;
	}

	public HiNodeIF node;

	public ValueType valueType;

	public HiClass valueClass;

	public HiClass originalValueClass;

	// Переменные со всеми возможными типами данных
	public byte byteNumber;

	public short shortNumber;

	public int intNumber;

	public float floatNumber;

	public long longNumber;

	public double doubleNumber;

	public char character;

	public boolean bool;

	public Object object; // HiObject or array (Obj[]...[] or <primitive>[]...[])

	public Object parentArray;

	public int arrayIndex;

	// Used in expressions as
	// field name, method name, class name or package name.
	// This is not a value, this is an intermediate condition.
	public String name;

	public int nameDimensions;

	public String castedVariableName;

	public HiNode castedCondition;

	public HiNode[] castedRecordArguments;

	public HiConstructor castedRecordArgumentsConstructor;

	public Type variableType;

	public HiField<?> variable;

	// Method arguments
	public HiNode[] arguments;

	/**
	 * Возвращает текущее значение в виде объекта в зависимости от типа.
	 */
	public Object get() {
		if (valueType == VARIABLE) {
			return variable.get();
		}

		// primitives
		if (valueClass.isPrimitive()) {
			PrimitiveType typeIndex = valueClass.getPrimitiveType();
			switch (typeIndex) {
				case BOOLEAN_TYPE:
					return bool;
				case CHAR_TYPE:
					return character;
				case BYTE_TYPE:
					return byteNumber;
				case SHORT_TYPE:
					return shortNumber;
				case INT_TYPE:
					return intNumber;
				case LONG_TYPE:
					return longNumber;
				case FLOAT_TYPE:
					return floatNumber;
				case DOUBLE_TYPE:
					return doubleNumber;
			}
		}
		return object;
	}

	public boolean set(Object value) {
		originalValueClass = null;
		if (value instanceof HiObject) {
			HiObject object = (HiObject) value;
			setObjectValue(object.clazz, object);
			return true;
		} else if (value == null) {
			setObjectValue(HiClass.OBJECT_CLASS, null);
			return true;
		} else if (value instanceof Value) {
			Value valueValue = (Value) value;
			valueValue.copyTo(this);
			return true;
		} else if (value instanceof String) {
			HiObject object = NodeString.createString(ctx, (String) value, false);
			setObjectValue(object.clazz, object);
			return true;
		} else {
			Class<?> clazz = value.getClass();
			if (clazz.isArray()) {
				setArrayValue(HiClass.getArrayElementClass(clazz), value);
				return true;
			} else if (clazz == Integer.class) {
				setIntValue((Integer) value);
				return true;
			} else if (clazz == Boolean.class) {
				setBooleanValue((Boolean) value);
				return true;
			} else if (clazz == Long.class) {
				setLongValue((Long) value);
				return true;
			} else if (clazz == Double.class) {
				setDoubleValue((Double) value);
				return true;
			} else if (clazz == Character.class) {
				setCharValue((Character) value);
				return true;
			} else if (clazz == Byte.class) {
				setByteValue((Byte) value);
				return true;
			} else if (clazz == Float.class) {
				setFloatValue((Float) value);
				return true;
			} else if (clazz == Short.class) {
				setShortValue((Short) value);
				return true;
			}
			return false;
		}
	}

	public Object getObject() {
		assert !valueClass.isPrimitive(); // checked in validation
		return object;
	}

	// @autoboxing
	public Object getObject(HiClass dstClass) {
		if (valueClass.isPrimitive()) {
			HiClassPrimitive primitiveClass = dstClass != null && dstClass.getAutoboxedPrimitiveClass() != null ? dstClass.getAutoboxedPrimitiveClass() : (HiClassPrimitive) valueClass;
			return primitiveClass.box(ctx, this); // changes ctx
		} else {
			return object;
		}
	}

	public String getStringValue(RuntimeContext ctx) {
		Object object = getObject();
		if (object instanceof HiObject) {
			return ((HiObject) object).getStringValue(ctx);
		}
		return null;
	}

	public Object getArray() {
		assert valueClass.isArray() || valueClass.isNull(); // checked in validation (array is expected)
		return object;
	}

	// @autoboxing
	public boolean getAutoboxPrimitiveValue(PrimitiveType expectedType) {
		if (valueClass.isPrimitive()) {
			if (valueClass.getPrimitiveType() == expectedType) {
				return true;
			}
		} else if (valueClass.getAutoboxedPrimitiveClass() != null) {
			PrimitiveType type = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
			if (type == expectedType) {
				if (object instanceof HiObject) {
					((HiObject) object).getAutoboxedValue(ctx, this);
					PrimitiveType autoboxType = valueClass.getPrimitiveType();
					if (autoboxType == expectedType) {
						return true;
					}
				} else if (object == null) {
					ctx.throwRuntimeException("null pointer");
					return false;
				}
			}
		}
		return false;
	}

	// @autoboxing
	public PrimitiveType getAutoboxValues(PrimitiveType... expectedTypes) {
		if (valueClass.isPrimitive()) {
			PrimitiveType type = valueClass.getPrimitiveType();
			for (int i = 0; i < expectedTypes.length; i++) {
				PrimitiveType expectedType = expectedTypes[i];
				if (type == expectedType) {
					return type;
				}
			}
		} else if (valueClass.getAutoboxedPrimitiveClass() != null) {
			PrimitiveType type = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
			for (int i = 0; i < expectedTypes.length; i++) {
				PrimitiveType expectedType = expectedTypes[i];
				if (type == expectedType) {
					if (object instanceof HiObject) {
						((HiObject) object).getAutoboxedValue(ctx, this);
						PrimitiveType autoboxType = valueClass.getPrimitiveType();
						if (autoboxType == expectedType) {
							return autoboxType;
						}
					} else {
						ctx.throwRuntimeException("null pointer");
					}
					return UNDEFINED_TYPE;
				}
			}
		}
		return UNDEFINED_TYPE;
	}

	// @autoboxing
	public void unbox() {
		if (valueClass.getAutoboxedPrimitiveClass() != null) {
			if (object != null) {
				substitutePrimitiveValueFromAutoboxValue();
				valueClass = valueClass.getAutoboxedPrimitiveClass();
			} else {
				ctx.throwRuntimeException("null pointer");
			}
		}
	}

	// @autoboxing
	public void substitutePrimitiveValueFromAutoboxValue() {
		if (object == null) {
			ctx.throwRuntimeException("null pointer");
			return;
		}
		if (object instanceof HiObject) {
			HiField valueField = ((HiObject) object).getField(ctx, "value");
			PrimitiveType t = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
			switch (t) {
				case BOOLEAN_TYPE:
					bool = (Boolean) valueField.get();
					break;
				case CHAR_TYPE:
					Object valueObject = valueField.get();
					if (valueObject instanceof Character) {
						character = (Character) valueObject;
					} else {
						character = (char) ((Number) valueField.get()).intValue();
					}
					break;
				case BYTE_TYPE:
					byteNumber = ((Number) valueField.get()).byteValue();
					break;
				case SHORT_TYPE:
					shortNumber = ((Number) valueField.get()).shortValue();
					break;
				case INT_TYPE:
					intNumber = ((Number) valueField.get()).intValue();
					break;
				case LONG_TYPE:
					longNumber = ((Number) valueField.get()).longValue();
					break;
				case FLOAT_TYPE:
					floatNumber = ((Number) valueField.get()).floatValue();
					break;
				case DOUBLE_TYPE:
					doubleNumber = ((Number) valueField.get()).doubleValue();
					break;
			}
		}
	}

	public boolean getBoolean() {
		if (getAutoboxPrimitiveValue(BOOLEAN_TYPE)) {
			return bool;
		} else if (ctx.exitFromBlock()) {
			// TODO delete?
			return false;
		}
		ctx.throwRuntimeException("boolean is expected");
		return false;
	}

	public char getChar() {
		if (getAutoboxPrimitiveValue(CHAR_TYPE)) {
			return character;
		} else if (ctx.exitFromBlock()) {
			return 0;
		}
		switch (valueClass.getPrimitiveType()) {
			case BYTE_TYPE:
				if (byteNumber >= Character.MIN_VALUE) {
					return (char) byteNumber;
				}
				break;
			case SHORT_TYPE:
				if (shortNumber >= Character.MIN_VALUE && shortNumber <= Character.MAX_VALUE) {
					return (char) shortNumber;
				}
				break;
			case INT_TYPE:
				if (intNumber >= Character.MIN_VALUE && intNumber <= Character.MAX_VALUE) {
					return (char) intNumber;
				}
				break;
		}
		ctx.throwRuntimeException("char is expected");
		return 0;
	}

	public byte getByte() {
		if (getAutoboxPrimitiveValue(BYTE_TYPE)) {
			return byteNumber;
		} else if (ctx.exitFromBlock()) {
			return 0;
		}
		switch (valueClass.getPrimitiveType()) {
			case SHORT_TYPE:
				if (shortNumber >= Byte.MIN_VALUE && shortNumber <= Byte.MAX_VALUE) {
					return (byte) shortNumber;
				}
				break;
			case CHAR_TYPE:
				if (character <= Byte.MAX_VALUE) {
					return (byte) character;
				}
				break;
			case INT_TYPE:
				if (intNumber >= Byte.MIN_VALUE && intNumber <= Byte.MAX_VALUE) {
					return (byte) intNumber;
				}
				break;
		}
		ctx.throwRuntimeException("byte is expected");
		return 0;
	}

	public short getShort() {
		if (getAutoboxPrimitiveValue(SHORT_TYPE)) {
			return shortNumber;
		} else if (ctx.exitFromBlock()) {
			return 0;
		}
		switch (valueClass.getPrimitiveType()) {
			case BYTE_TYPE:
				return byteNumber;
			case CHAR_TYPE:
				if (character <= Short.MAX_VALUE) {
					return (short) character;
				}
				break;
			case INT_TYPE:
				if (intNumber >= Short.MIN_VALUE && intNumber <= Short.MAX_VALUE) {
					return (short) intNumber;
				}
				break;
		}
		ctx.throwRuntimeException("short is expected");
		return 0;
	}

	public int getInt() {
		if (getAutoboxPrimitiveValue(INT_TYPE)) {
			return intNumber;
		} else if (ctx.exitFromBlock()) {
			return 0;
		}
		if (valueClass.isNull()) {
			ctx.throwRuntimeException("null pointer");
		} else {
			switch (valueClass.getPrimitiveType()) {
				case BYTE_TYPE:
					return byteNumber;
				case CHAR_TYPE:
					return character;
				case SHORT_TYPE:
					return shortNumber;
			}
			ctx.throwRuntimeException("int is expected");
		}
		return 0;
	}

	public long getLong() {
		PrimitiveType type = getAutoboxValues(BYTE_TYPE, SHORT_TYPE, CHAR_TYPE, INT_TYPE, LONG_TYPE);
		if (ctx.exitFromBlock()) {
			return 0;
		}
		if (type != UNDEFINED_TYPE) {
			switch (type) {
				case BYTE_TYPE:
					return byteNumber;
				case SHORT_TYPE:
					return shortNumber;
				case CHAR_TYPE:
					return character;
				case INT_TYPE:
					return intNumber;
				case LONG_TYPE:
					return longNumber;
			}
		}
		ctx.throwRuntimeException("long is expected");
		return 0;
	}

	public float getFloat() {
		PrimitiveType type = getAutoboxValues(BYTE_TYPE, SHORT_TYPE, CHAR_TYPE, INT_TYPE, LONG_TYPE, FLOAT_TYPE);
		if (ctx.exitFromBlock()) {
			return 0;
		}
		if (type != UNDEFINED_TYPE) {
			switch (type) {
				case BYTE_TYPE:
					return byteNumber;
				case SHORT_TYPE:
					return shortNumber;
				case CHAR_TYPE:
					return character;
				case INT_TYPE:
					return intNumber;
				case LONG_TYPE:
					return longNumber;
				case FLOAT_TYPE:
					return floatNumber;
			}
		}
		ctx.throwRuntimeException("float is expected");
		return 0;
	}

	public double getDouble() {
		PrimitiveType type = getAutoboxValues(BYTE_TYPE, SHORT_TYPE, CHAR_TYPE, INT_TYPE, LONG_TYPE, FLOAT_TYPE, DOUBLE_TYPE);
		if (ctx.exitFromBlock()) {
			return 0;
		}
		if (type != UNDEFINED_TYPE) {
			switch (type) {
				case BYTE_TYPE:
					return byteNumber;
				case SHORT_TYPE:
					return shortNumber;
				case CHAR_TYPE:
					return character;
				case INT_TYPE:
					return intNumber;
				case LONG_TYPE:
					return longNumber;
				case FLOAT_TYPE:
					return floatNumber;
				case DOUBLE_TYPE:
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
		dst.valueClass = valueClass;
		dst.originalValueClass = originalValueClass;
		dst.name = name;
		dst.nameDimensions = nameDimensions;

		// VALUE
		if (valueClass.isPrimitive()) {
			switch (valueClass.getPrimitiveType()) {
				case CHAR_TYPE:
					dst.character = character;
					break;
				case BYTE_TYPE:
					dst.byteNumber = byteNumber;
					break;
				case SHORT_TYPE:
					dst.shortNumber = shortNumber;
					break;
				case INT_TYPE:
					dst.intNumber = intNumber;
					break;
				case LONG_TYPE:
					dst.longNumber = longNumber;
					break;
				case FLOAT_TYPE:
					dst.floatNumber = floatNumber;
					break;
				case DOUBLE_TYPE:
					dst.doubleNumber = doubleNumber;
					break;
				case BOOLEAN_TYPE:
					dst.bool = bool;
					break;
			}
		} else {
			dst.bool = bool;
			dst.byteNumber = byteNumber;
			dst.character = character;
			dst.doubleNumber = doubleNumber;
			dst.floatNumber = floatNumber;
			dst.intNumber = intNumber;
			dst.longNumber = longNumber;
			dst.shortNumber = shortNumber;
			dst.object = object;
		}

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
		dst.castedRecordArgumentsConstructor = castedRecordArgumentsConstructor;
		dst.castedCondition = castedCondition;
	}

	public void copyToArray(Value value) {
		if (parentArray.getClass().getComponentType().isPrimitive()) {
			PrimitiveType typeIndex = valueClass.getPrimitiveType();
			switch (typeIndex) {
				case BOOLEAN_TYPE:
					Array.setBoolean(parentArray, arrayIndex, value.bool);
					break;
				case CHAR_TYPE:
					Array.setChar(parentArray, arrayIndex, value.character);
					break;
				case BYTE_TYPE:
					Array.setByte(parentArray, arrayIndex, value.byteNumber);
					break;
				case SHORT_TYPE:
					Array.setShort(parentArray, arrayIndex, value.shortNumber);
					break;
				case INT_TYPE:
					Array.setInt(parentArray, arrayIndex, value.intNumber);
					break;
				case LONG_TYPE:
					Array.setLong(parentArray, arrayIndex, value.longNumber);
					break;
				case FLOAT_TYPE:
					Array.setFloat(parentArray, arrayIndex, value.floatNumber);
					break;
				case DOUBLE_TYPE:
					Array.setDouble(parentArray, arrayIndex, value.doubleNumber);
					break;
			}
		} else {
			Array.set(parentArray, arrayIndex, value.object);
		}
	}

	public char[] getString(RuntimeContext ctx) {
		if (valueClass.isPrimitive()) {
			PrimitiveType t = valueClass.getPrimitiveType();
			switch (t) {
				case BOOLEAN_TYPE:
					return Boolean.toString(bool).toCharArray();
				case CHAR_TYPE:
					return Character.toString(character).toCharArray();
				case BYTE_TYPE:
					return Byte.toString(byteNumber).toCharArray();
				case SHORT_TYPE:
					return Short.toString(shortNumber).toCharArray();
				case INT_TYPE:
					return Integer.toString(intNumber).toCharArray();
				case LONG_TYPE:
					return Long.toString(longNumber).toCharArray();
				case FLOAT_TYPE:
					return Float.toString(floatNumber).toCharArray();
				case DOUBLE_TYPE:
					return Double.toString(doubleNumber).toCharArray();
			}
		}

		if (valueClass.isArray()) {
			if (object == null) {
				return NULL_CHARS;
			} else {
				HiClassArray arrayType = (HiClassArray) valueClass;
				return (arrayType.className + "@" + Integer.toHexString(object.hashCode())).toCharArray();
			}
		}

		if (valueClass.isNull() || object == null) {
			return NULL_CHARS;
		}

		HiObject object = (HiObject) this.object;
		boolean isString = object.clazz.fullName != null && object.clazz.isStringClass();
		if (isString) {
			return ImplUtil.getChars(ctx, object);
		}

		char[] toString = object.getStringChars(ctx);
		if (toString != null) {
			return toString;
		} else {
			return NULL_CHARS;
		}
	}

	@Override
	public String toString() {
		switch (valueType) {
			case VALUE:
				return "VALUE: type=" + valueClass + ", value=" + get();
			case TYPE:
				return "TYPE: variableType=" + variableType;
			case VARIABLE:
				return "VARIABLE: name=" + name + ", variable=" + variable;
			case CLASS:
				return "CLASS: type=" + valueClass + ", name=" + name;
			case METHOD_INVOCATION:
				return "METHOD: name=" + name + ", arguments=" + arguments;
			case ARRAY_INDEX:
				return "ARRAY INDEX: type=" + valueClass + ", parent array=" + parentArray + ", ara index=" + arrayIndex;
			case NAME:
//				public String name;
//				public int nameDimensions;

				if (castedRecordArguments != null || castedVariableName != null) {
					String text = "CASTED: " + variableType;
					if (castedRecordArguments != null) {
						text += "(";
						for (int i = 0; i < castedRecordArguments.length; i++) {
							if (i > 0) {
								text += ", ";
							}
							text += castedRecordArguments[i];
						}
						text += ")";
					}
					if (castedVariableName != null) {
						text += " " + castedVariableName;
					}
					if (castedCondition != null) {
						text += " when <condition>";
					}
					return text;
				} else {
					return "NAME: name=" + name + (nameDimensions > 0 ? "[]" : "");
				}
			case EXECUTE:
				return "EXECUTE: node=" + node;
			case TYPE_INVOCATION:
				return "TYPE INVOCATION: node=" + node;
		}
		return "";
	}

	public HiClass getOperationClass() {
		if (valueClass.getAutoboxedPrimitiveClass() != null) {
			// @autoboxing
			substitutePrimitiveValueFromAutoboxValue();
			return valueClass.getAutoboxedPrimitiveClass();
		} else if (object instanceof HiObject && valueClass.isObject()) {
			return ((HiObject) object).clazz;
		}
		return valueClass;
	}

	public void setObjectValue(HiClass clazz, HiObject value) {
		valueType = VALUE;
		valueClass = clazz;
		originalValueClass = value != null ? value.clazz : null;
		object = value;
	}

	public void setArrayValue(HiClass clazz, Object array) {
		assert clazz.isArray();
		valueType = VALUE;
		valueClass = clazz;
		originalValueClass = clazz;
		object = array;
	}

	public void setObjectOrArrayValue(HiClass clazz, HiClass originalValueClass, Object value) {
		valueType = VALUE;
		valueClass = clazz;
		this.originalValueClass = originalValueClass;
		object = value;
	}

	public void setVoidValue() {
		valueType = VALUE;
		valueClass = HiClassPrimitive.VOID;
	}

	public void setByteValue(byte value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.BYTE;
		byteNumber = value;
	}

	public void setShortValue(short value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.SHORT;
		shortNumber = value;
	}

	public void setCharValue(char value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.CHAR;
		character = value;
	}

	public void setIntValue(int value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.INT;
		intNumber = value;
	}

	public void setLongValue(long value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.LONG;
		longNumber = value;
	}

	public void setFloatValue(float value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.FLOAT;
		floatNumber = value;
	}

	public void setDoubleValue(double value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.DOUBLE;
		doubleNumber = value;
	}

	public void setBooleanValue(boolean value) {
		valueType = VALUE;
		valueClass = HiClassPrimitive.BOOLEAN;
		bool = value;
	}
}
