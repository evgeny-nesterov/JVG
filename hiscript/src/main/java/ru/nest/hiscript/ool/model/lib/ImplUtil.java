package ru.nest.hiscript.ool.model.lib;

import java.util.HashMap;
import java.util.Map;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Constructor;
import ru.nest.hiscript.ool.model.Field;
import ru.nest.hiscript.ool.model.Obj;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class ImplUtil {
	protected final static HashMap<RuntimeContext, Obj> threads = new HashMap<RuntimeContext, Obj>();

	private final static Map<Clazz, Obj> classes = new HashMap<Clazz, Obj>();

	private static Clazz classClazz;

	private static Constructor classConstructor;

	public static Clazz getClassClass(RuntimeContext ctx) {
		if (classClazz == null) {
			classClazz = Clazz.forName(ctx, "Class");
			classConstructor = classClazz.getConstructor(ctx);
		}
		return classClazz;
	}

	public static Constructor getClassConstructor(RuntimeContext ctx) {
		if (classClazz == null) {
			classClazz = Clazz.forName(ctx, "Class");
			classConstructor = classClazz.getConstructor(ctx);
		}
		return classConstructor;
	}

	public static Obj getClassObject(RuntimeContext ctx, Clazz clazz) {
		Obj classObject = classes.get(clazz);
		if (classObject == null) {
			classObject = getClassConstructor(ctx).newInstance(ctx, null, null);
			classObject.userObject = clazz;
			classes.put(clazz, classObject);
		}
		return classObject;
	}

	public static String getString(Obj str) {
		Field<?> field = str.getField("chars");
		return new String((char[]) field.get());
	}

	public static char[] getChars(Obj str) {
		Field<?> field = str.getField("chars");
		if (field != null) {
			return (char[]) field.get();
		} else {
			return null;
		}
	}

	protected static void returnVoid(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("void");
	}

	protected static void returnInt(RuntimeContext ctx, int value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("int");
		ctx.value.intNumber = value;
	}

	protected static void returnBoolean(RuntimeContext ctx, boolean value) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
		ctx.value.bool = value;
	}
}
