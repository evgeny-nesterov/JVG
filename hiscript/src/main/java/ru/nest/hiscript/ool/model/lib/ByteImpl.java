package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldByte;

// @autobox
public class ByteImpl extends ImplUtil {
	public static void Byte_int_hashCode(RuntimeContext ctx) {
		byte value = ((HiFieldByte) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Byte.hashCode(value));
	}
}
