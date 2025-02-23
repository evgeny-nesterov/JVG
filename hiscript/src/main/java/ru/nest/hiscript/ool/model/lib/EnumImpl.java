package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiEnumValue;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.classes.HiClassEnum;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

public class EnumImpl extends ImplUtil {
	public static void Enum_0E_values(RuntimeContext ctx) {
		HiClassEnum clazz = (HiClassEnum) ctx.value.valueClass;
		HiObject[] values = new HiObject[clazz.enumValues.size()];
		for (int i = 0; i < clazz.enumValues.size(); i++) {
			HiEnumValue value = clazz.enumValues.get(i);
			HiField enumField = clazz.getEnumValue(value.getName());
			enumField.get(ctx, ctx.value);
			values[i] = (HiObject) ctx.value.object;
		}
		returnArray(ctx, clazz.getArrayClass(), values);
	}

	public static void Enum_E_valueOf_String(RuntimeContext ctx, HiObject string) {
		String name = getString(ctx, string);
		HiClassEnum clazz = (HiClassEnum) ctx.value.valueClass;
		HiField enumField = clazz.getEnumValue(name);
		if (enumField != null) {
			enumField.get(ctx, ctx.value);
			returnObject(ctx, clazz, (HiObject) ctx.value.object);
		} else {
			returnObject(ctx, clazz, null);
		}
	}
}
