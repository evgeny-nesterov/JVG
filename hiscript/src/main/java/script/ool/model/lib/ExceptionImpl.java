package script.ool.model.lib;

import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.fields.FieldArray;

public class ExceptionImpl extends ImplUtil {
	public static void Exception_void_fillTrace(RuntimeContext ctx) {
		Obj object = ctx.value.object;
		FieldArray stackTrace = (FieldArray) object.getField("stackTrace");
		stackTrace.array = ctx.getNativeStack();
	}
}
