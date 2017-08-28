package script.ool.model.lib;

import script.ool.model.Clazz;
import script.ool.model.Field;
import script.ool.model.Obj;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.classes.ClazzArray;
import script.ool.model.nodes.NodeString;

public class ObjectImpl extends ImplUtil {
	public static void Object_int_hashCode(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("int");
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
				ClazzArray type = (ClazzArray) ctx.value.type;
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
		Obj src = ctx.value.object;
		Obj clone = clone(src);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.OBJECT_CLASS;
		ctx.value.object = clone;
	}

	public static Obj clone(Obj src) {
		Obj clone = new Obj(src.clazz, src.outboundObject);
		if (src.getSuperObject() != null) {
			clone.setSuperObject(clone(src.getSuperObject()));
		}

		if (src.fields != null) {
			int count = src.fields.length;
			clone.fields = new Field[count];
			for (int i = 0; i < count; i++) {
				clone.fields[i] = (Field<?>) src.fields[i].clone();
			}
		}
		return clone;
	}

	public static void Object_boolean_equals_Object(RuntimeContext ctx, Object obj) {
		if (ctx.value.type.isArray()) {
			ctx.value.bool = ctx.value.array == obj;
		} else {
			ctx.value.bool = ctx.value.object == obj;
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = Clazz.getPrimitiveClass("boolean");
	}

	public static void Object_void_wait(RuntimeContext ctx) {
		try {
			ctx.value.object.wait();
		} catch (Throwable e) {
			ctx.throwException(e.toString());
		}
		ctx.value.valueType = Value.VOID;
	}

	public static void Object_void_wait_long(RuntimeContext ctx, long timeout) {
		try {
			ctx.value.object.wait(timeout);
		} catch (Throwable e) {
			ctx.throwException(e.toString());
		}
		ctx.value.valueType = Value.VOID;
	}

	public static void Object_void_notify(RuntimeContext ctx) {
		try {
			ctx.value.object.notify();
		} catch (Throwable e) {
			ctx.throwException(e.getMessage());
		}
		ctx.value.valueType = Value.VOID;
	}

	public static void Object_void_notifyAll(RuntimeContext ctx) {
		try {
			ctx.value.object.notifyAll();
		} catch (Throwable e) {
			ctx.throwException(e.getMessage());
		}
		ctx.value.valueType = Value.VOID;
	}

	public static void Object_Class_getClass(RuntimeContext ctx) {
		Obj clazzObj = getClassObject(ctx, ctx.value.type);

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = clazzObj.clazz;
		ctx.value.object = clazzObj;
	}
}
