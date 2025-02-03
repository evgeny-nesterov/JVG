package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.fields.HiFieldChar;

// autobox
public class CharacterImpl extends ImplUtil {
	public static void Character_int_hashCode(RuntimeContext ctx) {
		char value = ((HiFieldChar) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Character.hashCode(value));
	}
}
