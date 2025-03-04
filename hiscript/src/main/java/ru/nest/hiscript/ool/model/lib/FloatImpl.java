package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldFloat;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class FloatImpl extends ImplUtil {
	public static void Float_int_hashCode(RuntimeContext ctx) {
		float value = ((HiFieldFloat) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Float.hashCode(value));
	}

	public static void Float_String_toString(RuntimeContext ctx) {
		float value = ((HiFieldFloat) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Float.toString(value));
	}

	public static void Float_boolean_equals_Object(RuntimeContext ctx, HiObject o) {
		if (o != null && o.clazz == HiClassPrimitive.FLOAT.getAutoboxClass()) {
			Float value1 = ((HiFieldFloat) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
			o.getField(ctx, "value").get(ctx, ctx.value);
			Float value2 = ctx.value.getFloat();
			returnBoolean(ctx, value1.equals(value2));
		} else {
			returnBoolean(ctx, false);
		}
	}
}
