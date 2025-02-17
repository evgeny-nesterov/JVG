package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldByte;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class ByteImpl extends ImplUtil {
	public static void Byte_int_hashCode(RuntimeContext ctx) {
		byte value = ((HiFieldByte) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Byte.hashCode(value));
	}

	public static void Byte_String_toString(RuntimeContext ctx) {
		byte value = ((HiFieldByte) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Byte.toString(value));
	}
}
