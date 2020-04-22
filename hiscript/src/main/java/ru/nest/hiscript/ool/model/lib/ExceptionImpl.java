package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.FieldArray;

public class ExceptionImpl extends ImplUtil {
	public static void Exception_void_fillTrace(RuntimeContext ctx) {
		Obj object = ctx.value.object;
		FieldArray stackTrace = (FieldArray) object.getField("stackTrace");
		stackTrace.array = ctx.getNativeStack();
	}
}
