package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class StringImpl extends ImplUtil {
	public static void String_int_length(RuntimeContext ctx) {
		returnInt(ctx, getChars(ctx, (HiObject) ctx.value.object).length);
	}

	public static void String_int_indexOf_int(RuntimeContext ctx, int ch) {
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).indexOf(ch));
	}

	public static void String_int_indexOf_String(RuntimeContext ctx, HiObject string) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).indexOf(s));
	}

	public static void String_int_indexOf_int_int(RuntimeContext ctx, int ch, int fromIndex) {
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).indexOf(ch, fromIndex));
	}

	public static void String_int_indexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).indexOf(s, fromIndex));
	}

	public static void String_int_lastIndexOf_int(RuntimeContext ctx, int ch) {
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).lastIndexOf(ch));
	}

	public static void String_int_lastIndexOf_String(RuntimeContext ctx, HiObject string) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).lastIndexOf(s));
	}

	public static void String_int_lastIndexOf_int_int(RuntimeContext ctx, int ch, int fromIndex) {
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).lastIndexOf(ch, fromIndex));
	}

	public static void String_int_lastIndexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).lastIndexOf(s, fromIndex));
	}

	public static void String_String_substring_int(RuntimeContext ctx, int beginIndex) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).substring(beginIndex));
	}

	public static void String_String_substring_int_int(RuntimeContext ctx, int beginIndex, int endIndex) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).substring(beginIndex, endIndex));
	}

	public static void String_String_toLowerCase(RuntimeContext ctx) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).toLowerCase());
	}

	public static void String_String_toUpperCase(RuntimeContext ctx) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).toUpperCase());
	}

	public static void String_String_replace_char_char(RuntimeContext ctx, char c1, char c2) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).replace(c1, c2));
	}

	public static void String_String_replace_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String target = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).replace(target, replacement));
	}

	public static void String_String_replaceAll_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String regex = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).replaceAll(regex, replacement));
	}

	public static void String_String_replaceFirst_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String regex = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).replaceFirst(regex, replacement));
	}

	public static void String_String_trim(RuntimeContext ctx) {
		NodeString.createString(ctx, getString(ctx, (HiObject) ctx.value.object).trim());
	}

	public static void String_char_charAt_int(RuntimeContext ctx, int index) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.CHAR.getArrayClass();
		ctx.value.object = getString(ctx, (HiObject) ctx.value.object).charAt(index);
	}

	public static void String_0char_toCharArray(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.CHAR.getArrayClass();
		ctx.value.object = getString(ctx, (HiObject) ctx.value.object).toCharArray();
	}

	public static void String_0byte_getBytes(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.valueClass = HiClassPrimitive.BYTE.getArrayClass();
		ctx.value.object = getString(ctx, (HiObject) ctx.value.object).getBytes();
	}

	public static void String_boolean_equals_Object(RuntimeContext ctx, HiObject o) {
		boolean equals = false;
		boolean isNull1 = ctx.value.object == null;
		boolean isNull2 = o == null;
		if (isNull1 || isNull2) {
			equals = isNull1 == isNull2;
		} else if (o.clazz.isStringClass()) {
			char[] chars1 = getChars(ctx, (HiObject) ctx.value.object);
			char[] chars2 = getChars(ctx, o);
			IF:
			if (chars1.length == chars2.length) {
				for (int i = 0; i < chars1.length; i++) {
					if (chars1[i] != chars2[i]) {
						break IF;
					}
				}
				equals = true;
			}
		}
		returnBoolean(ctx, equals);
	}

	public static void String_int_hashCode(RuntimeContext ctx) {
		returnInt(ctx, getString(ctx, (HiObject) ctx.value.object).hashCode());
	}
}
