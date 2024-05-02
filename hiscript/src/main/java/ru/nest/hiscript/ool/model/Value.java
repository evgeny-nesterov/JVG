package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
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

    public final static int TYPE_INVOCATION = 8; // for .class, .this, .super

    public RuntimeContext ctx;

    public Value(RuntimeContext ctx) {
        this.ctx = ctx;
    }

    public HiNodeIF node;

    public int valueType;

    public HiClass valueClass;

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

    public NodeArgument[] castedRecordArguments;

    public Type variableType;

    public HiField<?> variable;

    // Method arguments
    public HiNode[] arguments;

    public void clear() {
        node = null;
        valueClass = null;
        lambdaClass = null;
        object = null;
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

        // primitives
        if (valueClass.isPrimitive()) {
            int typeIndex = valueClass.getPrimitiveType();
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
                HiObject object = (HiObject) value;
                valueClass = object.clazz;
                this.object = object;
                return true;
            } else {
                object = null;
                valueClass = HiClass.OBJECT_CLASS;
                return true;
            }
        } else {
            // arrays
            Class<?> clazz = value.getClass();
            if (clazz.isArray()) {
                valueType = VALUE;
                this.valueClass = HiClass.getArrayType(clazz);
                object = value;
                return true;
            }
            // primitives
            else if (value instanceof Double) {
                valueType = VALUE;
                doubleNumber = (Double) value;
                this.valueClass = HiClassPrimitive.DOUBLE;
                return true;
            } else if (value instanceof Float) {
                valueType = VALUE;
                floatNumber = (Float) value;
                this.valueClass = HiClassPrimitive.FLOAT;
                return true;
            } else if (value instanceof Long) {
                valueType = VALUE;
                longNumber = (Long) value;
                this.valueClass = HiClassPrimitive.LONG;
                return true;
            } else if (value instanceof Integer) {
                valueType = VALUE;
                intNumber = (Integer) value;
                this.valueClass = HiClassPrimitive.INT;
                return true;
            } else if (value instanceof Short) {
                valueType = VALUE;
                shortNumber = (Short) value;
                this.valueClass = HiClassPrimitive.SHORT;
                return true;
            } else if (value instanceof Byte) {
                valueType = VALUE;
                byteNumber = (Byte) value;
                this.valueClass = HiClassPrimitive.BYTE;
                return true;
            } else if (value instanceof Character) {
                valueType = VALUE;
                character = (Character) value;
                this.valueClass = HiClassPrimitive.CHAR;
                return true;
            } else if (value instanceof Boolean) {
                valueType = VALUE;
                bool = (Boolean) value;
                this.valueClass = HiClassPrimitive.BOOLEAN;
                return true;
            }
            return false;
        }
    }

    public Object getObject() {
        if (valueClass.isPrimitive()) {
            ctx.throwRuntimeException("object is expected");
            return null;
        }
        return object;
    }

    // autobox
    public Object getObject(HiClass dstClass) {
        if (valueClass.isPrimitive()) {
            HiClassPrimitive primitiveClass = dstClass != null && dstClass.getAutoboxedPrimitiveClass() != null ? dstClass.getAutoboxedPrimitiveClass() : (HiClassPrimitive) valueClass;
            return primitiveClass.autobox(ctx, this); // changes ctx
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
        if (!valueClass.isArray() && !valueClass.isNull()) {
            ctx.throwRuntimeException("array is expected: " + valueClass.getNameDescr());
            return null;
        }
        return object;
    }

    // autobox
    public boolean getAutoboxPrimitiveValue(int expectedTypeIndex) {
        if (valueClass.isPrimitive()) {
            if (valueClass.getPrimitiveType() == expectedTypeIndex) {
                return true;
            }
        } else if (valueClass.getAutoboxedPrimitiveClass() != null) {
            int typeIndex = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
            if (typeIndex == expectedTypeIndex) {
                if (object instanceof HiObject) {
                    ((HiObject) object).getAutoboxedValue(ctx, this);
                    int autoboxTypeIndex = valueClass.getPrimitiveType();
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
        if (valueClass.isPrimitive()) {
            int typeIndex = valueClass.getPrimitiveType();
            for (int i = 0; i < expectedTypesIndexes.length; i++) {
                int expectedTypeIndex = expectedTypesIndexes[i];
                if (typeIndex == expectedTypeIndex) {
                    return typeIndex;
                }
            }
        } else if (valueClass.getAutoboxedPrimitiveClass() != null) {
            int typeIndex = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
            for (int i = 0; i < expectedTypesIndexes.length; i++) {
                int expectedTypeIndex = expectedTypesIndexes[i];
                if (typeIndex == expectedTypeIndex) {
                    if (object instanceof HiObject) {
                        ((HiObject) object).getAutoboxedValue(ctx, this);
                        int autoboxTypeIndex = valueClass.getPrimitiveType();
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
        if (valueClass.getAutoboxedPrimitiveClass() != null) {
            if (object != null) {
                substitutePrimitiveValueFromAutoboxValue();
                valueClass = valueClass.getAutoboxedPrimitiveClass();
            } else {
                ctx.throwRuntimeException("null pointer");
            }
        }
    }

    // autobox
    public void substitutePrimitiveValueFromAutoboxValue() {
        int t = valueClass.getAutoboxedPrimitiveClass().getPrimitiveType();
        if (object == null) {
            ctx.throwRuntimeException("null pointer");
            return;
        }
        if (object instanceof HiObject) {
            HiField valueField = ((HiObject) object).getField(ctx, "value");
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
        switch (valueClass.getPrimitiveType()) {
            case BYTE:
                if (byteNumber >= Character.MIN_VALUE) {
                    return (char) byteNumber;
                } else {
                    break;
                }
            case SHORT:
                if (shortNumber >= Character.MIN_VALUE && shortNumber <= Character.MAX_VALUE) {
                    return (char) shortNumber;
                } else {
                    break;
                }
            case INT:
                if (intNumber >= Character.MIN_VALUE && intNumber <= Character.MAX_VALUE) {
                    return (char) intNumber;
                } else {
                    break;
                }
        }
        ctx.throwRuntimeException("char is expected");
        return 0;
    }

    public byte getByte() {
        if (getAutoboxPrimitiveValue(BYTE)) {
            return byteNumber;
        }
        switch (valueClass.getPrimitiveType()) {
            case SHORT:
                if (shortNumber >= Byte.MIN_VALUE && shortNumber <= Byte.MAX_VALUE) {
                    return (byte) shortNumber;
                } else {
                    break;
                }
            case CHAR:
                if (character <= Byte.MAX_VALUE) {
                    return (byte) character;
                } else {
                    break;
                }
            case INT:
                if (intNumber >= Byte.MIN_VALUE && intNumber <= Byte.MAX_VALUE) {
                    return (byte) intNumber;
                } else {
                    break;
                }
        }
        ctx.throwRuntimeException("byte is expected");
        return 0;
    }

    public short getShort() {
        if (getAutoboxPrimitiveValue(SHORT)) {
            return shortNumber;
        }
        switch (valueClass.getPrimitiveType()) {
            case BYTE:
                return byteNumber;
            case CHAR:
                if (character <= Short.MAX_VALUE) {
                    return (short) character;
                } else {
                    break;
                }
            case INT:
                if (intNumber >= Short.MIN_VALUE && intNumber <= Short.MAX_VALUE) {
                    return (short) intNumber;
                } else {
                    break;
                }
        }
        ctx.throwRuntimeException("short is expected");
        return 0;
    }

    public int getInt() {
        if (getAutoboxPrimitiveValue(INT)) {
            return intNumber;
        }
        switch (valueClass.getPrimitiveType()) {
            case BYTE:
                return byteNumber;
            case CHAR:
                return character;
            case SHORT:
                return shortNumber;
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
        dst.valueClass = valueClass;
        dst.lambdaClass = lambdaClass;
        dst.name = name;
        dst.nameDimensions = nameDimensions;

        // VALUE
        if (valueClass.isPrimitive()) {
            switch (valueClass.getPrimitiveType()) {
                case CHAR:
                    dst.character = character;
                    break;
                case BYTE:
                    dst.byteNumber = byteNumber;
                    break;
                case SHORT:
                    dst.shortNumber = shortNumber;
                    break;
                case INT:
                    dst.intNumber = intNumber;
                    break;
                case LONG:
                    dst.longNumber = longNumber;
                    break;
                case FLOAT:
                    dst.floatNumber = floatNumber;
                    break;
                case DOUBLE:
                    dst.doubleNumber = doubleNumber;
                    break;
                case BOOLEAN:
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
        dst.castedCondition = castedCondition;
    }

    public void copyToArray(Value value) {
        // TODO: copy array and object

        int typeIndex = valueClass.getPrimitiveType();
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
        if (valueClass.isPrimitive()) {
            int t = valueClass.getPrimitiveType();
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

        if (valueClass.isArray()) {
            if (object == null) {
                return NULL;
            } else {
                HiClassArray arrayType = (HiClassArray) valueClass;
                return (arrayType.className + "@" + Integer.toHexString(object.hashCode())).toCharArray();
            }
        }

        if (valueClass.isNull() || object == null) {
            return NULL;
        }

        HiObject object = (HiObject) this.object;
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
                return "NAME: name=" + name + (nameDimensions > 0 ? "[]" : "");
            case EXECUTE:
                return "EXECUTE: node=" + node;
            case TYPE_INVOCATION:
                return "TYPE INVOCATION: node=" + node;
        }
        return "";
    }

    public HiClass getOperationClass() {
        if (valueClass.getAutoboxedPrimitiveClass() != null) {
            // autobox
            substitutePrimitiveValueFromAutoboxValue();
            return valueClass.getAutoboxedPrimitiveClass();
        } else if (object instanceof HiObject && valueClass.isObject()) {
            return ((HiObject) object).clazz;
        }
        return valueClass;
    }
}
