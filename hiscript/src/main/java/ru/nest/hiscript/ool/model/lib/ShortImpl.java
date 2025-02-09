package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldShort;

// @autobox
public class ShortImpl extends ImplUtil {
	public static void Short_int_hashCode(RuntimeContext ctx) {
		short value = ((HiFieldShort) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Short.hashCode(value));
	}
}
