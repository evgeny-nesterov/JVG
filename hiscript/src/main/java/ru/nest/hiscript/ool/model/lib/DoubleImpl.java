package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldDouble;

// @autobox
public class DoubleImpl extends ImplUtil {
    public static void Double_int_hashCode(RuntimeContext ctx) {
        double value = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
        returnInt(ctx, Double.hashCode(value));
    }
}
