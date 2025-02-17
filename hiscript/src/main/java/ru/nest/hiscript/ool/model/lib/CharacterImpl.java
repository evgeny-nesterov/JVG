package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.fields.HiFieldChar;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class CharacterImpl extends ImplUtil {
	public static void Character_int_hashCode(RuntimeContext ctx) {
		char value = ((HiFieldChar) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Character.hashCode(value));
	}

	public static void Character_String_toString(RuntimeContext ctx) {
		char value = ((HiFieldChar) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Character.toString(value));
	}
}
