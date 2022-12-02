package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class RecordImpl extends ImplUtil {
	public static void Record_boolean_equals_Object(RuntimeContext ctx, HiObject o) {
		// TODO
		boolean equals = false;
		boolean isNull1 = ctx.value.object == null;
		boolean isNull2 = o == null;
		if (isNull1 || isNull2) {
			equals = isNull1 == isNull2;
		} else if (o.clazz.fullName.equals("String")) {
			char[] chars1 = getChars(ctx.value.object);
			char[] chars2 = getChars(o);
			IF:
			if (chars1.length == chars2.length) {
				for (int i = 0; i < chars1.length; i++) {
					if (chars1[i] != chars2[i]) {
						break IF;
					}
				}
				equals = true;
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = equals;
	}

	public static void Record_int_hashCode(RuntimeContext ctx) {
		// TODO
		char[] chars = getChars(ctx.value.object);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = chars.length;
	}
}
