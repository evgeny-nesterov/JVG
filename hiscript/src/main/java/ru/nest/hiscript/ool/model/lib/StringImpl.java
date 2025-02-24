package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.JavaString;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;
import ru.nest.hiscript.ool.runtime.HiObject;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

public class StringImpl extends ImplUtil {
	public static void String_int_length(RuntimeContext ctx) {
		returnInt(ctx, getChars(ctx, (HiObject) ctx.value.object).length);
	}

	public static void String_int_indexOf_int(RuntimeContext ctx, int ch) {
		returnInt(ctx, getString(ctx).indexOf(ch));
	}

	public static void String_int_indexOf_String(RuntimeContext ctx, HiObject string) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx).indexOf(s));
	}

	public static void String_int_indexOf_int_int(RuntimeContext ctx, int ch, int fromIndex) {
		returnInt(ctx, getString(ctx).indexOf(ch, fromIndex));
	}

	public static void String_int_indexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx).indexOf(s, fromIndex));
	}

	public static void String_int_lastIndexOf_int(RuntimeContext ctx, int ch) {
		returnInt(ctx, getString(ctx).lastIndexOf(ch));
	}

	public static void String_int_lastIndexOf_String(RuntimeContext ctx, HiObject string) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx).lastIndexOf(s));
	}

	public static void String_int_lastIndexOf_int_int(RuntimeContext ctx, int ch, int fromIndex) {
		returnInt(ctx, getString(ctx).lastIndexOf(ch, fromIndex));
	}

	public static void String_int_lastIndexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s = getString(ctx, string);
		returnInt(ctx, getString(ctx).lastIndexOf(s, fromIndex));
	}

	public static void String_String_substring_int(RuntimeContext ctx, int beginIndex) {
		returnString(ctx, getString(ctx).substring(beginIndex));
	}

	public static void String_String_substring_int_int(RuntimeContext ctx, int beginIndex, int endIndex) {
		returnString(ctx, getString(ctx).substring(beginIndex, endIndex));
	}

	public static void String_String_toLowerCase(RuntimeContext ctx) {
		returnString(ctx, getString(ctx).toLowerCase());
	}

	public static void String_String_toUpperCase(RuntimeContext ctx) {
		returnString(ctx, getString(ctx).toUpperCase());
	}

	public static void String_String_replace_char_char(RuntimeContext ctx, char c1, char c2) {
		returnString(ctx, getString(ctx).replace(c1, c2));
	}

	public static void String_String_replace_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String target = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		returnString(ctx, getString(ctx).replace(target, replacement));
	}

	public static void String_String_replaceAll_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String regex = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		returnString(ctx, getString(ctx).replaceAll(regex, replacement));
	}

	public static void String_String_replaceFirst_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String regex = getString(ctx, s1);
		String replacement = getString(ctx, s2);
		returnString(ctx, getString(ctx).replaceFirst(regex, replacement));
	}

	public static void String_String_trim(RuntimeContext ctx) {
		returnString(ctx, getString(ctx).trim());
	}

	public static void String_char_charAt_int(RuntimeContext ctx, int index) {
		HiClass valueClass = HiClassPrimitive.CHAR.getArrayClass();
		char value = getString(ctx).charAt(index);
		returnChar(ctx, value);
	}

	public static void String_0char_toCharArray(RuntimeContext ctx) {
		HiClass valueClass = HiClassPrimitive.CHAR.getArrayClass();
		char[] value = getString(ctx).toCharArray();
		returnArray(ctx, valueClass, value);
	}

	public static void String_0byte_getBytes(RuntimeContext ctx) {
		HiClass valueClass = HiClassPrimitive.BYTE.getArrayClass();
		byte[] value = getString(ctx).getBytes();
		returnArray(ctx, valueClass, value);
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
		returnInt(ctx, getString(ctx).hashCode());
	}

	public static void String_String_intern(RuntimeContext ctx) {
		JavaString string = getJavaString(ctx);
		HiObject internString = NodeString.createString(ctx, string, true);
		returnObject(ctx, HiClass.STRING_CLASS, internString);
	}
}
