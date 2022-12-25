package ru.nest.hiscript.ool.model.lib;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeString;

public class StringImpl extends ImplUtil {
	public static void String_int_length(RuntimeContext ctx) {
		char[] chars = getChars(ctx, ctx.value.object);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = chars.length;
	}

	public static void String_int_indexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s1 = getString(ctx, ctx.value.object);
		String s2 = getString(ctx, string);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = s1.indexOf(s2, fromIndex);
	}

	public static void String_int_lastIndexOf_String_int(RuntimeContext ctx, HiObject string, int fromIndex) {
		String s1 = getString(ctx, ctx.value.object);
		String s2 = getString(ctx, string);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = s1.lastIndexOf(s2, fromIndex);
	}

	public static void String_String_substring_int_int(RuntimeContext ctx, int beginIndex, int endIndex) {
		String s = getString(ctx, ctx.value.object);
		s = s.substring(beginIndex, endIndex);
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_String_toLowerCase(RuntimeContext ctx) {
		String s = getString(ctx, ctx.value.object);
		s = s.toLowerCase();
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_String_toUpperCase(RuntimeContext ctx) {
		String s = getString(ctx, ctx.value.object);
		s = s.toUpperCase();
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_String_replace_char_char(RuntimeContext ctx, char c1, char c2) {
		String s = getString(ctx, ctx.value.object);
		s = s.replace(c1, c2);
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_String_replace_String_String(RuntimeContext ctx, HiObject s1, HiObject s2) {
		String s = getString(ctx, ctx.value.object);
		String _s1 = new String(getChars(ctx, s1));
		String _s2 = new String(getChars(ctx, s2));
		s = s.replace(_s1, _s2);
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_String_trim(RuntimeContext ctx) {
		String s = getString(ctx, ctx.value.object);
		s = s.trim();
		NodeString.createString(ctx, s.toCharArray());
	}

	public static void String_boolean_equals_Object(RuntimeContext ctx, HiObject o) {
		boolean equals = false;
		boolean isNull1 = ctx.value.object == null;
		boolean isNull2 = o == null;
		if (isNull1 || isNull2) {
			equals = isNull1 == isNull2;
		} else if (o.clazz.fullName.equals(HiClass.STRING_CLASS_NAME)) {
			char[] chars1 = getChars(ctx, ctx.value.object);
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

		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.BOOLEAN;
		ctx.value.bool = equals;
	}


	public static void String_int_hashCode(RuntimeContext ctx) {
		String s = getString(ctx, ctx.value.object);
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassPrimitive.INT;
		ctx.value.intNumber = s.hashCode();
	}
}
