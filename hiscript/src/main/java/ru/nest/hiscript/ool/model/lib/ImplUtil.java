package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;

import java.util.Collection;
import java.util.List;

public class ImplUtil {
	public static void removeThread(RuntimeContext ctx) {
		ctx.currentThread = null;
	}

	public static String getString(RuntimeContext ctx, HiObject string) {
		char[] chars = getChars(ctx, string);
		return chars != null ? new String(chars) : null;
	}

	public static char[] getChars(RuntimeContext ctx, HiObject string) {
		HiField<?> field = string != null ? string.getField(ctx, "chars") : null;
		if (field != null) {
			return (char[]) field.get();
		} else {
			return null;
		}
	}

	protected static void returnVoid(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.VOID;
	}

	protected static void returnInt(RuntimeContext ctx, int value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.INT;
		ctx.value.intNumber = value;
	}

	protected static void returnBoolean(RuntimeContext ctx, boolean value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = value;
	}

	protected static void returnArrayList(RuntimeContext ctx, Collection value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClass.forName(ctx, HiClass.ARRAYLIST_CLASS_NAME);
		ctx.value.lambdaClass = null;
		ctx.value.object = ctx.value.valueClass.searchConstructor(ctx).newInstance(ctx, null, null, null);
		((List) ((HiObject) ctx.value.object).userObject).addAll(value);
	}

	protected static void setCtx(RuntimeContext ctx, Object... objects) {
		for (Object object : objects) {
			if (object instanceof HiObject) {
				HiObject o = (HiObject) object;
				o.ctx = ctx; // used to compute hash code
			}
		}
	}
}
