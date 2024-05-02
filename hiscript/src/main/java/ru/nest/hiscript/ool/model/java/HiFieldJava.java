package ru.nest.hiscript.ool.model.java;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.lang.reflect.Field;

public class HiFieldJava extends HiField {
    private final Field field;

    public HiFieldJava(Field field, String name) {
        super((Type) null, name);
        this.field = field;
        field.setAccessible(true);
        type = HiJava.getTypeByJavaClass(field.getType());
        declared = true;
        initialized = true;
    }

    @Override
    public boolean isStatic() {
        return java.lang.reflect.Modifier.isStatic(field.getModifiers());
    }

    @Override
    protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
        return true;
    }

    @Override
    public void get(RuntimeContext ctx, Value value) {
        HiObject object = (HiObject) value.object;
        if (object.userObject == null) {
            ctx.throwRuntimeException("null pointer");
            return;
        }

        try {
            Object resultJavaValue = field.get(object.userObject);
            Object resultValue = HiJava.convertFromJava(ctx, resultJavaValue);
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
