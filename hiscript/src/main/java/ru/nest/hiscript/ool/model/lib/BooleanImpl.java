package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldBoolean;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class BooleanImpl extends ImplUtil {
	public static void Boolean_int_hashCode(RuntimeContext ctx) {
		boolean value = ((HiFieldBoolean) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Boolean.hashCode(value));
	}
}
