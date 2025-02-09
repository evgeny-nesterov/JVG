package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldFloat;

// @autobox
public class FloatImpl extends ImplUtil {
	public static void Float_int_hashCode(RuntimeContext ctx) {
		float value = ((HiFieldFloat) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Float.hashCode(value));
	}
}
