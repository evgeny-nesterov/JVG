package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldInt;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

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

	public static void Integer_String_toString_int(RuntimeContext ctx, int value) {
		returnString(ctx, Integer.toString(value));
	}
}
