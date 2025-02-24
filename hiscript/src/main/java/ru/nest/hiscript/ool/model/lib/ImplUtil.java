package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.JavaString;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

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

	public static String getString(RuntimeContext ctx) {
		return getString(ctx, (HiObject) ctx.value.object);
	}

	public static JavaString getJavaString(RuntimeContext ctx, HiObject string) {
		char[] chars = getChars(ctx, string);
		return chars != null ? new JavaString(chars) : null;
	}

	public static JavaString getJavaString(RuntimeContext ctx) {
		return getJavaString(ctx, (HiObject) ctx.value.object);
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
		ctx.value.setVoidValue();
	}

	protected static void returnByte(RuntimeContext ctx, byte value) {
		ctx.value.setByteValue(value);
	}

	protected static void returnShort(RuntimeContext ctx, short value) {
		ctx.value.setShortValue(value);
	}

	protected static void returnChar(RuntimeContext ctx, char value) {
		ctx.value.setCharValue(value);
	}

	protected static void returnInt(RuntimeContext ctx, int value) {
		ctx.value.setIntValue(value);
	}

	protected static void returnLong(RuntimeContext ctx, long value) {
		ctx.value.setLongValue(value);
	}

	protected static void returnFloat(RuntimeContext ctx, float value) {
		ctx.value.setFloatValue(value);
	}

	protected static void returnDouble(RuntimeContext ctx, double value) {
		ctx.value.setDoubleValue(value);
	}

	protected static void returnBoolean(RuntimeContext ctx, boolean value) {
		ctx.value.setBooleanValue(value);
	}

	protected static void returnArrayList(RuntimeContext ctx, Collection value) {
		HiClass valueClass = HiClass.forName(ctx, HiClass.ARRAYLIST_CLASS_NAME);
		HiObject object = valueClass.searchConstructor(ctx).newInstance(ctx, null, null, null);
		ctx.value.setObjectValue(valueClass, object);
		((List) object.userObject).addAll(value);
	}

	protected static void returnString(RuntimeContext ctx, String value) {
		if (value != null) {
			ctx.value.setObjectValue(HiClass.STRING_CLASS, NodeString.createString(ctx, value, false));
		} else {
			ctx.value.setObjectValue(HiClass.STRING_CLASS, null);
		}
	}

	protected static void returnObject(RuntimeContext ctx, HiClass clazz, HiObject value) {
		ctx.value.setObjectValue(clazz, value);
	}

	protected static void returnArray(RuntimeContext ctx, HiClass clazz, Object array) {
		ctx.value.setArrayValue(clazz, array);
	}

	protected static void returnObjectOrArray(RuntimeContext ctx, HiClass clazz, HiClass originalValueClass, Object value) {
		ctx.value.setObjectOrArrayValue(clazz, originalValueClass, value);
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
