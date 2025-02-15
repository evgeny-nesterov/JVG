package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldInt;

// @autoboxing
public class IntegerImpl extends ImplUtil {
	public static void Integer_int_hashCode(RuntimeContext ctx) {
		int value = ((HiFieldInt) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Integer.hashCode(value));
	}

	public static void Integer_String_toString(RuntimeContext ctx) {
		int value = ((HiFieldInt) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Integer.toString(value));
	}
}
