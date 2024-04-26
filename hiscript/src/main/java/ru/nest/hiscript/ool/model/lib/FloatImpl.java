package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldFloat;

// autobox
public class FloatImpl extends ImplUtil {
	public static void Float_int_hashCode(RuntimeContext ctx) {
		float value = ((HiFieldFloat) ctx.value.object.getField(ctx, "value")).getValue();
		returnInt(ctx, Float.hashCode(value));
	}
}
