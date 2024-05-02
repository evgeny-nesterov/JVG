package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Array;

public class HiFieldArray extends HiField<Object> {
    public HiFieldArray(Type type, String name) {
        super(type, name);
    }

    public HiFieldArray(HiClass clazz, String name) {
        super(clazz, name);
    }

    public Object array;

    public HiClass arrayType;

    @Override
    protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
        return valueType.clazz.isNull() || (valueType.clazz.isArray() && HiClass.autoCast(ctx, valueType.clazz, fieldClass, false, true));
    }

    @Override
    public void get(RuntimeContext ctx, Value value) {
        value.valueType = Value.VALUE;
        value.valueClass = arrayType != null ? arrayType : getClass(ctx);
        value.object = array;
    }

    @Override
    public void set(RuntimeContext ctx, Value value) {
        declared = true;
        if (value.valueClass == HiClassNull.NULL) {
            array = null;
            arrayType = getClass(ctx);
        } else {
            array = value.object;
            arrayType = value.valueClass;
        }
        initialized = true;
    }

    @Override
    public Object get() {
        return array;
    }

    @Override
    public Object getJava(RuntimeContext ctx) {
        return getJava(ctx, arrayType, array);
    }

    public static Object getJava(RuntimeContext ctx, HiClass arrayType, Object array) {
        Class javaClass = arrayType.getJavaClass();
        if (javaClass != null) {
            Class rootCellClass = javaClass;
            while (rootCellClass.isArray()) {
                rootCellClass = rootCellClass.getComponentType();
            }
            if (rootCellClass.isPrimitive()) {
                return array;
            } else {
                return getJavaArray(ctx, array, javaClass.getComponentType());
            }
        }
        return null;
    }

    private static Object getJavaArray(RuntimeContext ctx, Object array, Class cellClass) {
        int length = Array.getLength(array);
        Object javaArray = Array.newInstance(cellClass, length);
        for (int i = 0; i < length; i++) {
            Object cellValue = Array.get(array, i);
            if (cellValue == null) {
                continue;
            }
            Object javaCellValue;
            if (cellClass.isArray()) {
                javaCellValue = getJavaArray(ctx, cellValue, cellClass.getComponentType());
            } else {
                HiObject cellObject = (HiObject) cellValue;
                javaCellValue = cellObject.getJavaValue(ctx);
            }
            Array.set(array, i, javaCellValue);
        }
        return javaArray;
    }
}
