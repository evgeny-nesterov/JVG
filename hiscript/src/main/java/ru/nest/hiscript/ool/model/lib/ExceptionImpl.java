package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldArray;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

public class ExceptionImpl extends ImplUtil {
    public static void Exception_void_fillTrace(RuntimeContext ctx) {
        HiObject object = (HiObject) ctx.value.object;
        HiFieldArray stackTrace = (HiFieldArray) object.getField(ctx, "stackTrace");
        stackTrace.array = ctx.getNativeStack();
    }
}
