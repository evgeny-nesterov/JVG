package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.*;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImplUtil {
    private final static Map<HiClass, HiObject> classes = new ConcurrentHashMap<>();

    private static HiClass classClass;

    private static HiConstructor classConstructor;

    public static void removeThread(RuntimeContext ctx) {
        ctx.currentThread = null;
    }

    public static HiClass getClassClass(ClassResolver ctx) {
        if (classClass == null) {
            classClass = HiClass.forName(ctx, "Class");
            classConstructor = classClass.getConstructor(ctx);
        }
        classClass.init(ctx);
        return classClass;
    }

    public static HiConstructor getClassConstructor(RuntimeContext ctx) {
        if (classClass == null) {
            classClass = HiClass.forName(ctx, "Class");
            classConstructor = classClass.getConstructor(ctx);
        }
        return classConstructor;
    }

    public static HiObject getClassObject(RuntimeContext ctx, HiClass clazz) {
        HiObject classObject = classes.get(clazz);
        if (classObject == null) {
            classObject = getClassConstructor(ctx).newInstance(ctx, null, null, null);
            classObject.userObject = clazz;
            classes.put(clazz, classObject);
        }
        return classObject;
    }

    public static String getString(RuntimeContext ctx, HiObject string) {
        char[] chars = getChars(ctx, string);
        return chars != null ? new String(chars) : null;
    }

    public static char[] getChars(RuntimeContext ctx, HiObject string) {
        HiField<?> field = string != null ? string.getField(ctx, "chars") : null;
        if (field != null) {
            return (char[]) field.get();
        } else {
            return null;
        }
    }

    protected static void returnVoid(RuntimeContext ctx) {
        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClassPrimitive.VOID;
    }

    protected static void returnInt(RuntimeContext ctx, int value) {
        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClassPrimitive.INT;
        ctx.value.intNumber = value;
    }

    protected static void returnBoolean(RuntimeContext ctx, boolean value) {
        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
        ctx.value.bool = value;
    }

    protected static void returnArrayList(RuntimeContext ctx, Collection value) {
        ctx.value.valueType = Value.VALUE;
        ctx.value.valueClass = HiClass.forName(ctx, HiClass.ARRAYLIST_CLASS_NAME);
        ctx.value.lambdaClass = null;
        ctx.value.object = ctx.value.valueClass.searchConstructor(ctx).newInstance(ctx, null, null, null);
        ((List) ((HiObject) ctx.value.object).userObject).addAll(value);
    }
}
