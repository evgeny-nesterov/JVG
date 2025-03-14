package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassNull;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

import java.lang.reflect.Array;

public class HiFieldArray extends HiField<Object> {
    public HiFieldArray(Type type, String name) {
        super(type, name);
    }

    public HiFieldArray(HiClass clazz, String name) {
        super(clazz, name);
    }

    public Object array;

    public HiClass arrayClass;

    @Override
    protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
        return valueType.clazz.isNull() || (valueType.clazz.isArray() && HiClass.autoCast(ctx, valueType.clazz, fieldClass, false, true));
    }

    @Override
    public void get(RuntimeContext ctx, Value value) {
        HiClass valueClass = arrayClass != null ? arrayClass : getClass(ctx);
        value.setArrayValue(valueClass, array);
    }

    @Override
    public void set(RuntimeContext ctx, Value value) {
        declared = true;
        if (value.valueClass == HiClassNull.NULL) {
            array = null;
            arrayClass = getClass(ctx);
        } else {
            array = value.object;
            arrayClass = value.valueClass;
        }
        initialized = true;
    }

    @Override
    public void set(Object array, HiClass arrayClass) {
		this.declared = true;
		this.array = array;
		this.arrayClass = arrayClass;
		this.initialized = true;
	}

    @Override
    public Object get() {
        return array;
    }

    @Override
    public Object getJava(RuntimeContext ctx) {
        return getJava(ctx, arrayClass, array);
    }

    public static Object getJava(RuntimeContext ctx, HiClass arrayClass, Object array) {
        Class javaClass = arrayClass.getJavaClass(ctx.getEnv());
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
            } else if(cellValue instanceof HiObject) {
                HiObject cellObject = (HiObject) cellValue;
                javaCellValue = cellObject.getJavaValue(ctx);
            } else {
                javaCellValue = cellValue;
            }
            Array.set(javaArray, i, javaCellValue);
        }
        return javaArray;
    }
}
