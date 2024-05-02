package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class ObjectImpl extends ImplUtil {
    public static void Object_int_hashCode(RuntimeContext ctx) {
        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClassPrimitive.INT;
        if (ctx.value.valueClass.isArray()) {
            ctx.value.intNumber = ctx.value.object.hashCode();
        } else {
            ctx.value.intNumber = ((HiObject) ctx.value.object).hashCode(ctx);
        }
    }

    public static void Object_String_toString(RuntimeContext ctx) {
        String text;
        if (ctx.value.valueClass.isArray()) {
            if (ctx.value.object != null) {
                HiClassArray type = (HiClassArray) ctx.value.valueClass;
                text = type.className + "@" + Integer.toHexString(ctx.value.object.hashCode());
            } else {
                text = "null";
            }
        } else {
            if (ctx.value.object != null) {
                text = ((HiObject) ctx.value.object).toString(ctx);
            } else {
                text = "null";
            }
        }
        NodeString.createString(ctx, text);
    }

    public static void Object_Object_clone(RuntimeContext ctx) {
        HiObject src = (HiObject) ctx.value.object;
        HiObject clone = clone(src);

        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClass.OBJECT_CLASS;
        ctx.value.lambdaClass = null;
        ctx.value.object = clone;
    }

    public static HiObject clone(HiObject src) {
        HiObject clone = new HiObject(src.clazz, src.type, src.outboundObject);
        if (src.getSuperObject() != null) {
            clone.setSuperObject(clone(src.getSuperObject()));
        }

        if (src.fields != null) {
            int count = src.fields.length;
            clone.fields = new HiField[count];
            for (int i = 0; i < count; i++) {
                clone.fields[i] = (HiField<?>) src.fields[i].clone();
            }
        }
        return clone;
    }

    public static void Object_boolean_equals_Object(RuntimeContext ctx, Object obj) {
        if (ctx.value.valueClass.isArray()) {
            ctx.value.bool = ctx.value.object == obj;
        } else {
            ctx.value.bool = ctx.value.object == obj;
        }

        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
    }

    public static void Object_void_wait(RuntimeContext ctx) {
        try {
            ctx.value.object.wait();
        } catch (Throwable e) {
            ctx.throwRuntimeException(e.toString());
        }
        ctx.value.valueType = PrimitiveTypes.VOID;
    }

    public static void Object_void_wait_long(RuntimeContext ctx, long timeout) {
        try {
            ctx.value.object.wait(timeout);
        } catch (Throwable e) {
            ctx.throwRuntimeException(e.toString());
        }
        ctx.value.valueType = PrimitiveTypes.VOID;
    }

    public static void Object_void_notify(RuntimeContext ctx) {
        try {
            ctx.value.object.notify();
        } catch (Throwable e) {
            ctx.throwRuntimeException(e.getMessage());
        }
        ctx.value.valueType = PrimitiveTypes.VOID;
    }

    public static void Object_void_notifyAll(RuntimeContext ctx) {
        try {
            ctx.value.object.notifyAll();
        } catch (Throwable e) {
            ctx.throwRuntimeException(e.getMessage());
        }
        ctx.value.valueType = PrimitiveTypes.VOID;
    }

    public static void Object_Class_getClass(RuntimeContext ctx) {
        HiObject clazzObj = getClassObject(ctx, ctx.value.valueClass);

        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = clazzObj.clazz;
        ctx.value.lambdaClass = null;
        ctx.value.object = clazzObj;
    }
}
