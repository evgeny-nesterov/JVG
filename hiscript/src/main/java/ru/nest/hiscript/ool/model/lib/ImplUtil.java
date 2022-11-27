package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiConstructor;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.util.HashMap;
import java.util.Map;

public class ImplUtil {
	protected final static HashMap<RuntimeContext, HiObject> threads = new HashMap<>();

	private final static Map<HiClass, HiObject> classes = new HashMap<>();

	private static HiClass classClass;

	private static HiConstructor classConstructor;

	public static HiClass getClassClass(RuntimeContext ctx) {
		if (classClass == null) {
			classClass = HiClass.forName(ctx, "Class");
			classConstructor = classClass.getConstructor(ctx);
		}
		return classClass;
	}

	public static HiConstructor getClassConstructor(RuntimeContext ctx) {
		if (classClass == null) {
			classClass = HiClass.forName(ctx, "Class");
			classConstructor = classClass.getConstructor(ctx);
		}
		return classConstructor;
	}

	public static HiObject getClassObject(RuntimeContext ctx, HiClass clazz) {
		HiObject classObject = classes.get(clazz);
		if (classObject == null) {
			classObject = getClassConstructor(ctx).newInstance(ctx, null, null);
			classObject.userObject = clazz;
			classes.put(clazz, classObject);
		}
		return classObject;
	}

	public static String getString(HiObject str) {
		HiField<?> field = str.getField("chars");
		return new String((char[]) field.get());
	}

	public static char[] getChars(HiObject str) {
		HiField<?> field = str != null ? str.getField("chars") : null;
		if (field != null) {
			return (char[]) field.get();
		} else {
			return null;
		}
	}

	protected static void returnVoid(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("void");
	}

	protected static void returnInt(RuntimeContext ctx, int value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = value;
	}

	protected static void returnBoolean(RuntimeContext ctx, boolean value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = value;
	}
}
