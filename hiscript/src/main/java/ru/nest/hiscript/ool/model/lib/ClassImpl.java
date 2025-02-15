package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class ClassImpl extends ImplUtil {
	public static void Class_String_getName(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		NodeString.createString(ctx, clazz.name);
	}

	public static void Class_String_getFullName(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		NodeString.createString(ctx, clazz.fullName);
	}

	public static void Class_Class_forName_String(RuntimeContext ctx, HiObject string) {
		String name = getString(ctx, string);
		HiClass clazz = HiClass.forName(ctx, name);
		if (clazz == null) {
			String prefix = HiClass.ROOT_CLASS_NAME + "$0";
			if (!name.startsWith(prefix)) {
				clazz = HiClass.forName(ctx, prefix + name);
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = ctx.getClassLoader().getClassClass(ctx);
		ctx.value.originalValueClass = null;
		if (clazz != null) {
			ctx.value.object = ctx.getClassLoader().getClassObject(ctx, clazz);
		} else {
			ctx.value.object = null;
		}
	}

	public static void Class_boolean_isArray(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isArray());
	}

	public static void Class_boolean_isPrimitive(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isPrimitive());
	}

	public static void Class_boolean_isInterface(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isInterface);
	}

	public static void Class_boolean_isEnum(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isEnum());
	}

	public static void Class_boolean_isAnnotation(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isAnnotation());
	}

	public static void Class_boolean_isAnonymousClass(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isAnonymous());
	}

	public static void Class_boolean_isLocalClass(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isLocal());
	}

	public static void Class_boolean_isMemberClass(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		returnBoolean(ctx, clazz.isInner());
	}

	public static void Class_Class_getComponentType(RuntimeContext ctx) {
		HiClass clazz = (HiClass) ((HiObject) ctx.value.object).userObject;
		if (!clazz.isArray()) {
			ctx.throwRuntimeException("class is not represent an array");
		}

		HiClassArray arrayClazz = (HiClassArray) clazz;
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = ctx.getClassLoader().getClassClass(ctx);
		ctx.value.originalValueClass = null;
		ctx.value.object = ctx.getClassLoader().getClassObject(ctx, arrayClazz.cellClass);
	}
}
