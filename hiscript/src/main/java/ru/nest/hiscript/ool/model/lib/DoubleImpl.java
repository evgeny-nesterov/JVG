package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.fields.HiFieldDouble;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

// @autoboxing
public class DoubleImpl extends ImplUtil {
	public static void Double_int_hashCode(RuntimeContext ctx) {
		double value = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnInt(ctx, Double.hashCode(value));
	}

	public static void Double_String_toString(RuntimeContext ctx) {
		double value = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
		returnString(ctx, Double.toString(value));
	}

	public static void Double_boolean_equals_Object(RuntimeContext ctx, HiObject o) {
		if (o != null && o.clazz == HiClassPrimitive.DOUBLE.getAutoboxClass()) {
			Double value1 = ((HiFieldDouble) ((HiObject) ctx.value.object).getField(ctx, "value")).get();
			o.getField(ctx, "value").get(ctx, ctx.value);
			Double value2 = ctx.value.getDouble();
			returnBoolean(ctx, value1.equals(value2));
		} else {
			returnBoolean(ctx, false);
		}
	}
}
