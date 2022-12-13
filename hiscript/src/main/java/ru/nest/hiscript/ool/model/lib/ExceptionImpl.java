package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldArray;

public class ExceptionImpl extends ImplUtil {
	public static void Exception_void_fillTrace(RuntimeContext ctx) {
		HiObject object = ctx.value.object;
		HiFieldArray stackTrace = (HiFieldArray) object.getField(ctx, "stackTrace");
		stackTrace.array = ctx.getNativeStack();
	}
}
