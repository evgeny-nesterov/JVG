package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldLong;

// @autobox
public class LongImpl extends ImplUtil {
	public static void Long_int_hashCode(RuntimeContext ctx) {
		long value = ((HiFieldLong) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Long.hashCode(value));
	}
}
