package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldLong;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class LongImpl extends ImplUtil {
	public static void Long_int_hashCode(RuntimeContext ctx) {
		long value = ((HiFieldLong) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Long.hashCode(value));
	}

	public static void Long_String_toString(RuntimeContext ctx) {
		long value = ((HiFieldLong) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Long.toString(value));
	}
}
