package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class ClassImpl extends ImplUtil {
	public static void Class_String_getName(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ctx.value.object.userObject;
		NodeString.createString(ctx, clazz.name.toCharArray());
	}

	public static void Class_String_getFullName(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ctx.value.object.userObject;
		NodeString.createString(ctx, clazz.fullName.toCharArray());
	}

	public static void Class_Class_forName_String(RuntimeContext ctx, HiObject string) {
		String name = getString(string);
		HiClass clazz = HiClass.forName(ctx, name);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = getClassClass(ctx);
		if (clazz != null) {
			ctx.value.object = getClassObject(ctx, clazz);
		} else {
			ctx.value.object = null;
		}
	}
}
