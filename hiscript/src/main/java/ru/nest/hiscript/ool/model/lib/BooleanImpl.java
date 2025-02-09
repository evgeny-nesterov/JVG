package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldBoolean;

// @autobox
public class BooleanImpl extends ImplUtil {
	public static void Boolean_int_hashCode(RuntimeContext ctx) {
		boolean value = ((HiFieldBoolean) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Boolean.hashCode(value));
	}
}
