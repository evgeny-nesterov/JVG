package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldDouble;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class DoubleImpl extends ImplUtil {
    public static void Double_int_hashCode(RuntimeContext ctx) {
        double value = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
        returnInt(ctx, Double.hashCode(value));
    }

    public static void Double_String_toString(RuntimeContext ctx) {
        double value = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
        returnString(ctx, Double.toString(value));
    }
}
