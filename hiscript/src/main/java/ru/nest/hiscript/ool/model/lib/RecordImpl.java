package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;

import java.util.Objects;

public class RecordImpl extends ImplUtil {
	public static void Record_boolean_equals_Object(RuntimeContext ctx, HiObject o2) {
		HiClassRecord clazz = (HiClassRecord) ctx.value.type;
		if (clazz != o2.clazz) {
			ctx.value.valueType = Value.VALUE;
			ctx.value.type = HiClass.getPrimitiveClass("boolean");
			ctx.value.bool = false;
			return;
		}

		HiObject o1 = ctx.value.object;
		for (NodeArgument field : clazz.defaultConstructor.arguments) {
			HiField f1 = o1.getField(field.name);
			HiField f2 = o2.getField(field.name);
			Object v1 = f1.get();
			Object v2 = f2.get();
			if (v1 instanceof HiObject) {
				if (!(v2 instanceof HiObject)) {
					ctx.value.valueType = Value.VALUE;
					ctx.value.type = HiClass.getPrimitiveClass("boolean");
					ctx.value.bool = false;
					return;
				}
				HiObject vo1 = (HiObject) v1;
				HiObject vo2 = (HiObject) v2;
				if (!vo1.equals(ctx, vo2)) {
					ctx.value.valueType = Value.VALUE;
					ctx.value.type = HiClass.getPrimitiveClass("boolean");
					ctx.value.bool = false;
					return;
				}
			} else if (f1.type.isPrimitive()) {
				if (!f2.type.isPrimitive() || !Objects.equals(v1, v2)) {
					ctx.value.valueType = Value.VALUE;
					ctx.value.type = HiClass.getPrimitiveClass("boolean");
					ctx.value.bool = false;
					return;
				}
			} else if (f1.type.isArray()) {
				if (!f2.type.isArray() || v1 != v2) {
					ctx.value.valueType = Value.VALUE;
					ctx.value.type = HiClass.getPrimitiveClass("boolean");
					ctx.value.bool = false;
					return;
				}
			} else if (f1.type.isNull()) {
				if (!f2.type.isNull()) {
					ctx.value.valueType = Value.VALUE;
					ctx.value.type = HiClass.getPrimitiveClass("boolean");
					ctx.value.bool = false;
					return;
				}
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("boolean");
		ctx.value.bool = true;
	}

	public static void Record_int_hashCode(RuntimeContext ctx) {
		HiClassRecord clazz = (HiClassRecord) ctx.value.type;
		HiObject object = ctx.value.object;
		int[] hashCodes = new int[clazz.defaultConstructor.arguments.length];
		for (int i = 0; i < clazz.defaultConstructor.arguments.length; i++) {
			HiField field = object.getField(clazz.defaultConstructor.arguments[i].name);
			Object value = field.get();
			if (value instanceof HiObject) {
				HiObject vo = (HiObject) value;
				hashCodes[i] = vo.hashCode(ctx);
			} else if (field.type.isPrimitive() || field.type.isArray()) {
				hashCodes[i] = value.hashCode();
			}
		}

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClass.getPrimitiveClass("int");
		ctx.value.intNumber = Objects.hash(hashCodes);
	}
}
