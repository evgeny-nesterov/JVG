package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiField;
import ru.nest.hiscript.ool.model.classes.HiClassRecord;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.util.Objects;

public class RecordImpl extends ImplUtil {
	public static void Record_boolean_equals_Object(RuntimeContext ctx, HiObject o2) {
		HiClass valueClass = ctx.value.valueClass;
		if (!valueClass.isRecord()) {
			returnBoolean(ctx, false);
			return;
		}
		HiClassRecord clazz = (HiClassRecord) valueClass;
		if (clazz != o2.clazz) {
			returnBoolean(ctx, false);
			return;
		}
		HiObject o1 = (HiObject) ctx.value.object;
		for (HiField f1 : o1.fields) {
			HiField f2 = o2.getField(ctx, f1.name);
			Object v1 = f1.get();
			Object v2 = f2.get();
			if (v1 instanceof HiObject) {
				if (!(v2 instanceof HiObject)) {
					returnBoolean(ctx, false);
					return;
				}
				HiObject vo1 = (HiObject) v1;
				HiObject vo2 = (HiObject) v2;
				if (!vo1.equals(ctx, vo2)) {
					returnBoolean(ctx, false);
					return;
				}
			} else if (f1.type.isPrimitive()) {
				if (!f2.type.isPrimitive() || !Objects.equals(v1, v2)) {
					returnBoolean(ctx, false);
					return;
				}
			} else if (f1.type.isArray()) {
				if (!f2.type.isArray() || v1 != v2) {
					returnBoolean(ctx, false);
					return;
				}
			} else if (f1.type.isNull()) {
				if (!f2.type.isNull()) {
					returnBoolean(ctx, false);
					return;
				}
			}
		}
		returnBoolean(ctx, true);
	}

	public static void Record_int_hashCode(RuntimeContext ctx) {
		HiObject object = (HiObject) ctx.value.object;
		int[] hashCodes = new int[object.fields.length];
		for (int i = 0; i < object.fields.length; i++) {
			HiField field = object.fields[i];
			Object value = field.get();
			if (value instanceof HiObject) {
				HiObject valueObject = (HiObject) value;
				hashCodes[i] = valueObject.hashCode(ctx);
			} else if (field.type.isPrimitive() || field.type.isArray()) {
				hashCodes[i] = value.hashCode();
			} else { // null
				hashCodes[i] = -1;
			}
		}
		returnInt(ctx, Objects.hash(hashCodes));
	}

	public static void Record_String_toString(RuntimeContext ctx) {
		HiObject object = (HiObject) ctx.value.object;
		StringBuilder string = new StringBuilder().append(object.clazz.getNameDescr()).append('[');
		for (int i = 0; i < object.fields.length; i++) {
			HiField field = object.fields[i];
			field.get(ctx, ctx.value);
			if (i > 0) {
				string.append(", ");
			}
			string.append(field.getVariableName());
			string.append('=');
			string.append(field.get());
		}
		string.append("]");
		returnString(ctx, string.toString());
	}
}
