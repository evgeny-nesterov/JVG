package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.PrimitiveTypes;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassArray;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class ObjectImpl extends ImplUtil {
	public static void Object_int_hashCode(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		if (ctx.value.type.isArray()) {
			ctx.value.intNumber = ctx.value.array.hashCode();
		} else {
			ctx.value.object.ctx = ctx;
			ctx.value.intNumber = ctx.value.object.hashCode();
		}
	}

	public static void Object_String_toString(RuntimeContext ctx) {
		String text;
		if (ctx.value.type.isArray()) {
			if (ctx.value.array != null) {
				HiClassArray type = (HiClassArray) ctx.value.type;
				text = type.className + "@" + Integer.toHexString(ctx.value.array.hashCode());
			} else {
				text = "null";
			}
		} else {
			if (ctx.value.object != null) {
				ctx.value.object.ctx = ctx;
				text = ctx.value.object.toString();
			} else {
				text = "null";
			}
		}
		NodeString.createString(ctx, text.toCharArray());
	}

	public static void Object_Object_clone(RuntimeContext ctx) {
		HiObject src = ctx.value.object;
		HiObject clone = clone(src);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.OBJECT_CLASS;
		ctx.value.object = clone;
	}

	public static HiObject clone(HiObject src) {
		HiObject clone = new HiObject(src.clazz, src.outboundObject);
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
		if (ctx.value.type.isArray()) {
			ctx.value.bool = ctx.value.array == obj;String s;
		} else {
			ctx.value.bool = ctx.value.object == obj;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
	}

	public static void Object_void_wait(RuntimeContext ctx) {
		try {
			ctx.value.object.wait();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.toString());
		}
		ctx.value.valueType = PrimitiveTypes.VOID;
	}

	public static void Object_void_wait_long(RuntimeContext ctx, long timeout) {
		try {
			ctx.value.object.wait(timeout);
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.toString());
		}
		ctx.value.valueType = PrimitiveTypes.VOID;
	}

	public static void Object_void_notify(RuntimeContext ctx) {
		try {
			ctx.value.object.notify();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.getMessage());
		}
		ctx.value.valueType = PrimitiveTypes.VOID;
	}

	public static void Object_void_notifyAll(RuntimeContext ctx) {
		try {
			ctx.value.object.notifyAll();
		} catch (Throwable e) {
			ctx.throwRuntimeException(e.getMessage());
		}
		ctx.value.valueType = PrimitiveTypes.VOID;
	}

	public static void Object_Class_getClass(RuntimeContext ctx) {
		HiObject clazzObj = getClassObject(ctx, ctx.value.type);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazzObj.clazz;
		ctx.value.object = clazzObj;
	}
}
