import org.junit.jupiter.api.Test;

public class TestString extends HiTest {
	@Test
	public void testString() {
		// declaration
		assertSuccessSerialize("String s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccessSerialize("String s; s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccessSerialize("String s = \"\"; assert s.length() == 0; assert s.equals(\"\");");
		assertSuccessSerialize("String s = null; assert s == null;");
		assertSuccessSerialize("String s = new String(\"abc\"); assert s.equals(\"abc\");");
		assertSuccessSerialize("String s1 = \"abc\"; String s2 = new String(s1); assert s2.equals(\"abc\");");
		assertSuccessSerialize("String s = \"xxx\\nyyy\"; assert s.indexOf(\"\\n\") == 3;");
		assertSuccessSerialize("String s = \"'\";");
		assertFailCompile("String s = 1;", //
				"incompatible types: int cannot be converted to String");
		assertFailCompile("String s = 'a';", //
				"incompatible types: char cannot be converted to String");
		assertFailCompile("String s = true;", //
				"incompatible types: boolean cannot be converted to String");

		// special symbols
		assertSuccessSerialize("String s = \"quote=\\\"\"; assert s.indexOf('\"') == 6;");
		assertFailCompile("String s = \"\\s\";", //
				"illegal escape character");
		assertFailCompile("String s = \"a\nb\";", //
				"Illegal line end in string literal");
		assertFailCompile("String s = \"a\rb\";", //
				"Illegal line end in string literal");

		// plus
		assertSuccessSerialize("String s = \"\" + 1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = 1 + \"\"; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\" + (byte)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = (byte)1 + \"\"; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\" + (short)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = (short)1 + \"\"; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\" + (long)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = (long)1 + \"\"; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\" + (float)1; assert s.equals(\"1.0\");");
		assertSuccessSerialize("String s = 1f + \"\"; assert s.equals(\"1.0\");");
		assertSuccessSerialize("String s = \"\" + (double)1; assert s.equals(\"1.0\");");
		assertSuccessSerialize("String s = 1.2345 + \"\"; assert s.equals(\"1.2345\");");
		assertSuccessSerialize("String s = \"a\" + 'b'; assert s.equals(\"ab\");");
		assertSuccessSerialize("String s = 'a' + \"b\"; assert s.equals(\"ab\");");
		assertSuccessSerialize("String s = \"\" + true; assert s.equals(\"true\");");
		assertSuccessSerialize("String s = true + \"\"; assert s.equals(\"true\");");
		assertSuccessSerialize("String s = \"ab\" + \"cd\"; assert s.equals(\"abcd\");");
		assertSuccessSerialize("String s = \"\" + null; assert s.equals(\"null\");");
		assertSuccessSerialize("String s = null + \"\"; assert s.equals(\"null\");");
		assertSuccessSerialize("String s = \"\" + \"\"; assert s.equals(\"\");");

		// +=
		assertSuccessSerialize("String s = \"\"; s += \"A\"; assert s.equals(\"A\");");
		assertSuccessSerialize("String s = \"\"; s += \"\"; assert s.equals(\"\");");
		assertSuccessSerialize("String s = null; s += \"A\"; assert s.equals(\"nullA\");");
		assertSuccessSerialize("String s = \"A\"; s += null; assert s.equals(\"Anull\");");
		assertSuccessSerialize("String s = null; s += null; assert s.equals(\"nullnull\");");
		assertSuccessSerialize("String s = \"\"; s += 1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\"; s += (byte)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\"; s += (short)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\"; s += (long)1; assert s.equals(\"1\");");
		assertSuccessSerialize("String s = \"\"; s += 1f; assert s.equals(\"1.0\");");
		assertSuccessSerialize("String s = \"\"; s += 1.0; assert s.equals(\"1.0\");");
		assertSuccessSerialize("String s = \"\"; s += 'a'; assert s.equals(\"a\");");
		assertSuccessSerialize("String s = \"\"; s += false; assert s.equals(\"false\");");

		// indexOf
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf('s') == 2;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf(\"string\") == 5;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf('s', 4) == 5;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf(\"string\", 5) == 5;");

		// lastIndexOf
		assertSuccessSerialize("String s = \"test test\"; assert s.lastIndexOf('e') == 6;");
		assertSuccessSerialize("String s = \"test test\"; assert s.lastIndexOf(\"test\") == 5;");

		assertSuccessSerialize("char[] chars = \"test string\".toCharArray(); assert chars.length == 11;");
		assertSuccessSerialize("byte[] bytes = \"abc\".getBytes(); assert bytes.length == 3;");

		// substring
		assertSuccessSerialize("assert \"abcd\".substring(1, 3).equals(\"bc\");");
		assertSuccessSerialize("assert \"abcd\".substring(2).equals(\"cd\");");

		assertSuccessSerialize("assert \" abc \".trim().equals(\"abc\");");
		assertSuccessSerialize("assert \"abc\".toUpperCase().equals(\"ABC\");");
		assertSuccessSerialize("assert \"ABC\".toLowerCase().equals(\"abc\");");

		// complex
		assertSuccessSerialize("for(char c : \"abc\".toCharArray());");

		// value
		assertSuccessSerialize("assert \"a\".charAt(0) == 'a';");
		assertSuccessSerialize("assert \"\\u0000\".charAt(0) == 0;");
		assertSuccessSerialize("assert \"\\uffff\".charAt(0) == '\\uffff';");
		assertSuccessSerialize("assert \"\\n\".charAt(0) == '\\n';");

		// equals
		assertSuccessSerialize("assert \"123\".toString().equals(\"123\");");

		// toString
		assertFail("String s = null; s.toString();", //
				"null pointer");
	}

	@Test
	public void testBlocks() {
		assertSuccessSerialize("String s = \"\"\"\nabc\nabc\"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccessSerialize("String s = \"\"\"\n   abc\n   abc\"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccessSerialize("String s = \"\"\"\nabc\n abc\"\"\"; assert s.equals(\"abc\\n abc\");");
		assertSuccessSerialize("String s = \"\"\"\n abc\n  abc\"\"\"; assert s.equals(\"abc\\n abc\");");
		assertSuccessSerialize("String s = \"\"\"\n abc\nabc\"\"\"; assert s.equals(\" abc\\nabc\");");
		assertSuccessSerialize("String s = \"\"\"\n  abc\n abc\"\"\"; assert s.equals(\" abc\\nabc\");");
		assertSuccessSerialize("String s = \"\"\"\nabc   \nabc   \"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccessSerialize("String s = \"\"\"\nabc   \\s\nabc   \\s  \"\"\"; assert s.equals(\"abc   \\nabc   \");");
		assertSuccessSerialize("String s = \"\"\"\n   \\\"\"\"   \"\"\"; assert s.equals(\"\\\"\\\"\\\"\");");
		assertSuccessSerialize("String s = \"\"\"\n a\\\n b \\\n c \"\"\"; assert s.equals(\"ab c\");");
		assertFailCompile("String s = \"\"\";", //
				"new line is expected");
		assertFailCompile("String s = \"\"\"\";", //
				"new line is expected");
		assertFailCompile("String s = \"\"\"\n;", //
				"'\"\"\"' is expected");
		assertFailCompile("String s = \"\"\"\n\";", //
				"'\"\"\"' is expected");
		assertFailCompile("String s = \"\"\"\n\"\";", //
				"'\"\"\"' is expected");
		assertFailCompile("String s = \"\"\"\n\\\"\"\";", //
				"'\"\"\"' is expected");
	}

	@Test
	public void testChars() {
		assertSuccessSerialize("String s = \"\\n\"; assert s.charAt(0) == '\\n';");
		assertSuccessSerialize("String s = \"\\t\"; assert s.charAt(0) == '\\t';");
		assertSuccessSerialize("String s = \"\\r\"; assert s.charAt(0) == '\\r';");
		assertSuccessSerialize("String s = \"\\b\"; assert s.charAt(0) == '\\b';");
		assertSuccessSerialize("String s = \"\\f\"; assert s.charAt(0) == '\\f';");
		assertSuccessSerialize("String s = \"\\\"\"; assert s.charAt(0) == '\\\"';");
		assertSuccessSerialize("String s = \"\\'\"; assert s.charAt(0) == '\\'';");
		assertSuccessSerialize("String s = \"\\\\\"; assert s.charAt(0) == '\\\\';");
		assertFailCompile("String s = \"\\\";", //
				"'\"' is expected");
		assertFailCompile("char c1 = 'a;\nchar c2 = 'a';", //
				"' is expected");

		// octal
		assertSuccessSerialize("String s = \"\\7\"; assert s.charAt(0) == '\\7';");
		assertSuccessSerialize("String s = \"\\01\"; assert s.charAt(0) == '\\1';");
		assertSuccessSerialize("String s = \"\\123\"; assert s.charAt(0) == '\\123';");
		assertSuccessSerialize("String s = \"\\045\"; assert s.charAt(0) == '\\045';");
		assertSuccessSerialize("String s = \"\\067\"; assert s.charAt(0) == '\\067';");
		assertSuccessSerialize("String s = \"\\777\"; assert s.charAt(0) == '\\77' && s.charAt(1) == '7';");
		assertSuccessSerialize("String s = \"\\1234\"; assert s.charAt(0) == '\\123' && s.charAt(1) == '4';");
		assertFailCompile("String s = \"\\8\";", //
				"illegal escape character");

		// hex
		assertSuccessSerialize("String s = \"\\u0123\"; assert s.charAt(0) == 0x123;");
		assertSuccessSerialize("String s = \"\\u4567\"; assert s.charAt(0) == 0x4567;");
		assertSuccessSerialize("String s = \"\\u89ab\"; assert s.charAt(0) == 0x89ab;");
		assertSuccessSerialize("String s = \"\\ucdef\"; assert s.charAt(0) == 0xcdef;");
		assertFailCompile("String s = \"\\u\";", //
				"invalid code of character");
		assertFailCompile("String s = \"\\u0\";", //
				"invalid code of character");
		assertFailCompile("String s = \"\\u01\";", //
				"invalid code of character");
		assertFailCompile("String s = \"\\u012\";", //
				"invalid code of character");
		assertFailCompile("String s = \"\\u000g\";", //
				"invalid code of character");
	}

	@Test
	public void testCharsMethods() {
		assertSuccessSerialize("String s = \"abc\"; assert s.length() == 3;");
		assertSuccessSerialize("String s = \"abc\"; assert s.indexOf(\"bc\") == 1; assert s.indexOf(\"d\") == -1;");
		assertSuccessSerialize("String s = \"abc\"; assert s.lastIndexOf(\"bc\") == 1; assert s.lastIndexOf(\"d\") == -1;");
	}
}