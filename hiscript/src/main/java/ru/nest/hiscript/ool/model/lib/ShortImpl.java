package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldShort;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class ShortImpl extends ImplUtil {
	public static void Short_int_hashCode(RuntimeContext ctx) {
		short value = ((HiFieldShort) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Short.hashCode(value));
	}

	public static void Short_String_toString(RuntimeContext ctx) {
		short value = ((HiFieldShort) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Short.toString(value));
	}
}
