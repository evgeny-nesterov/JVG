package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

public class ObjectImpl extends ImplUtil {
	public static void Object_int_hashCode(RuntimeContext ctx) {
		if (ctx.value.valueClass.isArray()) {
			returnInt(ctx, ctx.value.object.hashCode());
		} else {
			returnInt(ctx, ((HiObject) ctx.value.object).hashCode(ctx));
		}
	}

	public static void Object_String_toString(RuntimeContext ctx) {
		String text;
		if (ctx.value.valueClass.isArray()) {
			if (ctx.value.object != null) {
				HiClassArray type = (HiClassArray) ctx.value.valueClass;
				text = type.className + "@" + Integer.toHexString(ctx.value.object.hashCode());
			} else {
				text = "null";
			}
		} else {
			if (ctx.value.object != null) {
				text = ((HiObject) ctx.value.object).toString(ctx);
			} else {
				text = "null";
			}
		}
		NodeString.createString(ctx, text, false);
	}

	public static void Object_Object_clone(RuntimeContext ctx) {
		HiObject src = (HiObject) ctx.value.object;
		returnObject(ctx, HiClass.OBJECT_CLASS, clone(src));
	}

	public static HiObject clone(HiObject src) {
		HiObject clone = new HiObject(src.ctx, src.clazz, src.type, src.outboundObject);
		if (src.getSuperObject() != null) {
			clone.setSuperObject(clone(src.getSuperObject()));
		}

		if (src.fields != null) {
			int count = src.fields.length;
			clone.fields = new HiField[count];
			for (int i = 0; i < count; i++) {
				clone.fields[i] = (HiField<?>) src.fields[i].clone();
			}
		}
		return clone;
	}

	public static void Object_boolean_equals_Object(RuntimeContext ctx, Object obj) {
		returnBoolean(ctx, ctx.value.object == obj);
	}

	public static void Object_void_wait(RuntimeContext ctx) {
		try {
			ctx.value.object.wait();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.toString());
		}
		returnVoid(ctx);
	}

	public static void Object_void_wait_long(RuntimeContext ctx, long timeout) {
		try {
			ctx.value.object.wait(timeout);
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.toString());
		}
		returnVoid(ctx);
	}

	public static void Object_void_notify(RuntimeContext ctx) {
		try {
			ctx.value.object.notify();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.getMessage());
		}
		returnVoid(ctx);
	}

	public static void Object_void_notifyAll(RuntimeContext ctx) {
		try {
			ctx.value.object.notifyAll();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.getMessage());
		}
		returnVoid(ctx);
	}

	public static void Object_Class_getClass(RuntimeContext ctx) {
		HiObject clazzObj = ctx.getClassLoader().getClassObject(ctx, ctx.value.valueClass);
		returnObject(ctx, clazzObj.clazz, clazzObj);
	}
}
