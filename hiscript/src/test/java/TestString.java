import org.junit.jupiter.api.Test;

public class TestString extends HiTest {
	@Test
	public void testString() {
		// declaration
		assertSuccess("String s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccess("String s; s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccess("String s = \"\"; assert s.length() == 0; assert s.equals(\"\");");
		assertSuccess("String s = null; assert s == null;");
		assertSuccess("String s = new String(\"abc\"); assert s.equals(\"abc\");");
		assertSuccess("String s1 = \"abc\"; String s2 = new String(s1); assert s2.equals(\"abc\");");
		assertSuccess("String s = \"xxx\\nyyy\"; assert s.indexOf(\"\\n\") == 3;");
		assertSuccess("String s = \"'\";");
		assertFailCompile("String s = 1;", //
				"incompatible types: int cannot be converted to String");
		assertFailCompile("String s = 'a';", //
				"incompatible types: char cannot be converted to String");
		assertFailCompile("String s = true;", //
				"incompatible types: boolean cannot be converted to String");

		// special symbols
		assertSuccess("String s = \"quote=\\\"\"; assert s.indexOf('\"') == 6;");
		assertFailCompile("String s = \"\\s\";", //
				"illegal escape character");
		assertFailCompile("String s = \"a\nb\";", //
				"Illegal line end in string literal");
		assertFailCompile("String s = \"a\rb\";", //
				"Illegal line end in string literal");

		// plus
		assertSuccess("String s = \"\" + 1; assert s.equals(\"1\");");
		assertSuccess("String s = 1 + \"\"; assert s.equals(\"1\");");
		assertSuccess("String s = \"\" + (byte)1; assert s.equals(\"1\");");
		assertSuccess("String s = (byte)1 + \"\"; assert s.equals(\"1\");");
		assertSuccess("String s = \"\" + (short)1; assert s.equals(\"1\");");
		assertSuccess("String s = (short)1 + \"\"; assert s.equals(\"1\");");
		assertSuccess("String s = \"\" + (long)1; assert s.equals(\"1\");");
		assertSuccess("String s = (long)1 + \"\"; assert s.equals(\"1\");");
		assertSuccess("String s = \"\" + (float)1; assert s.equals(\"1.0\");");
		assertSuccess("String s = 1f + \"\"; assert s.equals(\"1.0\");");
		assertSuccess("String s = \"\" + (double)1; assert s.equals(\"1.0\");");
		assertSuccess("String s = 1.2345 + \"\"; assert s.equals(\"1.2345\");");
		assertSuccess("String s = \"a\" + 'b'; assert s.equals(\"ab\");");
		assertSuccess("String s = 'a' + \"b\"; assert s.equals(\"ab\");");
		assertSuccess("String s = \"\" + true; assert s.equals(\"true\");");
		assertSuccess("String s = true + \"\"; assert s.equals(\"true\");");
		assertSuccess("String s = \"ab\" + \"cd\"; assert s.equals(\"abcd\");");
		assertSuccess("String s = \"\" + null; assert s.equals(\"null\");");
		assertSuccess("String s = null + \"\"; assert s.equals(\"null\");");
		assertSuccess("String s = \"\" + \"\"; assert s.equals(\"\");");

		// +=
		assertSuccess("String s = \"\"; s += \"A\"; assert s.equals(\"A\");");
		assertSuccess("String s = \"\"; s += \"\"; assert s.equals(\"\");");
		assertSuccess("String s = null; s += \"A\"; assert s.equals(\"nullA\");");
		assertSuccess("String s = \"A\"; s += null; assert s.equals(\"Anull\");");
		assertSuccess("String s = null; s += null; assert s.equals(\"nullnull\");");
		assertSuccess("String s = \"\"; s += 1; assert s.equals(\"1\");");
		assertSuccess("String s = \"\"; s += (byte)1; assert s.equals(\"1\");");
		assertSuccess("String s = \"\"; s += (short)1; assert s.equals(\"1\");");
		assertSuccess("String s = \"\"; s += (long)1; assert s.equals(\"1\");");
		assertSuccess("String s = \"\"; s += 1f; assert s.equals(\"1.0\");");
		assertSuccess("String s = \"\"; s += 1.0; assert s.equals(\"1.0\");");
		assertSuccess("String s = \"\"; s += 'a'; assert s.equals(\"a\");");
		assertSuccess("String s = \"\"; s += false; assert s.equals(\"false\");");

		// indexOf
		assertSuccess("String s = \"test string\"; assert s.indexOf('s') == 2;");
		assertSuccess("String s = \"test string\"; assert s.indexOf(\"string\") == 5;");
		assertSuccess("String s = \"test string\"; assert s.indexOf('s', 4) == 5;");
		assertSuccess("String s = \"test string\"; assert s.indexOf(\"string\", 5) == 5;");

		// lastIndexOf
		assertSuccess("String s = \"test test\"; assert s.lastIndexOf('e') == 6;");
		assertSuccess("String s = \"test test\"; assert s.lastIndexOf(\"test\") == 5;");

		assertSuccess("char[] chars = \"test string\".toCharArray(); assert chars.length == 11;");
		assertSuccess("byte[] bytes = \"abc\".getBytes(); assert bytes.length == 3;");

		// substring
		assertSuccess("assert \"abcd\".substring(1, 3).equals(\"bc\");");
		assertSuccess("assert \"abcd\".substring(2).equals(\"cd\");");

		assertSuccess("assert \" abc \".trim().equals(\"abc\");");
		assertSuccess("assert \"abc\".toUpperCase().equals(\"ABC\");");
		assertSuccess("assert \"ABC\".toLowerCase().equals(\"abc\");");

		// complex
		assertSuccess("for(char c : \"abc\".toCharArray());");

		// value
		assertSuccess("assert \"a\".charAt(0) == 'a';");
		assertSuccess("assert \"\\u0000\".charAt(0) == 0;");
		assertSuccess("assert \"\\uffff\".charAt(0) == '\\uffff';");
		assertSuccess("assert \"\\n\".charAt(0) == '\\n';");

		// equals
		assertSuccess("assert \"123\".toString().equals(\"123\");");

		// toString
		assertFail("String s = null; s.toString();", //
				"null pointer");
	}

	@Test
	public void testBlocks() {
		assertSuccess("String s = \"\"\"\nabc\nabc\"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccess("String s = \"\"\"\n   abc\n   abc\"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccess("String s = \"\"\"\nabc\n abc\"\"\"; assert s.equals(\"abc\\n abc\");");
		assertSuccess("String s = \"\"\"\n abc\n  abc\"\"\"; assert s.equals(\"abc\\n abc\");");
		assertSuccess("String s = \"\"\"\n abc\nabc\"\"\"; assert s.equals(\" abc\\nabc\");");
		assertSuccess("String s = \"\"\"\n  abc\n abc\"\"\"; assert s.equals(\" abc\\nabc\");");
		assertSuccess("String s = \"\"\"\nabc   \nabc   \"\"\"; assert s.equals(\"abc\\nabc\");");
		assertSuccess("String s = \"\"\"\nabc   \\s\nabc   \\s  \"\"\"; assert s.equals(\"abc   \\nabc   \");");
		assertSuccess("String s = \"\"\"\n   \\\"\"\"   \"\"\"; assert s.equals(\"\\\"\\\"\\\"\");");
		assertSuccess("String s = \"\"\"\n a\\\n b \\\n c \"\"\"; assert s.equals(\"ab c\");");
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
		assertSuccess("String s = \"\\n\"; assert s.charAt(0) == '\\n';");
		assertSuccess("String s = \"\\t\"; assert s.charAt(0) == '\\t';");
		assertSuccess("String s = \"\\r\"; assert s.charAt(0) == '\\r';");
		assertSuccess("String s = \"\\b\"; assert s.charAt(0) == '\\b';");
		assertSuccess("String s = \"\\f\"; assert s.charAt(0) == '\\f';");
		assertSuccess("String s = \"\\\"\"; assert s.charAt(0) == '\\\"';");
		assertSuccess("String s = \"\\'\"; assert s.charAt(0) == '\\'';");
		assertSuccess("String s = \"\\\\\"; assert s.charAt(0) == '\\\\';");
		assertFailCompile("String s = \"\\\";", //
				"'\"' is expected");
		assertFailCompile("char c1 = 'a;\nchar c2 = 'a';", //
				"' is expected");

		// octal
		assertSuccess("String s = \"\\7\"; assert s.charAt(0) == '\\7';");
		assertSuccess("String s = \"\\01\"; assert s.charAt(0) == '\\1';");
		assertSuccess("String s = \"\\123\"; assert s.charAt(0) == '\\123';");
		assertSuccess("String s = \"\\045\"; assert s.charAt(0) == '\\045';");
		assertSuccess("String s = \"\\067\"; assert s.charAt(0) == '\\067';");
		assertSuccess("String s = \"\\777\"; assert s.charAt(0) == '\\77' && s.charAt(1) == '7';");
		assertSuccess("String s = \"\\1234\"; assert s.charAt(0) == '\\123' && s.charAt(1) == '4';");
		assertFailCompile("String s = \"\\8\";", //
				"illegal escape character");

		// hex
		assertSuccess("String s = \"\\u0123\"; assert s.charAt(0) == 0x123;");
		assertSuccess("String s = \"\\u4567\"; assert s.charAt(0) == 0x4567;");
		assertSuccess("String s = \"\\u89ab\"; assert s.charAt(0) == 0x89ab;");
		assertSuccess("String s = \"\\ucdef\"; assert s.charAt(0) == 0xcdef;");
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
		assertSuccess("String s = \"abc\"; assert s.length() == 3;");
		assertSuccess("String s = \"abc\"; assert s.indexOf(\"bc\") == 1; assert s.indexOf(\"d\") == -1;");
		assertSuccess("String s = \"abc\"; assert s.lastIndexOf(\"bc\") == 1; assert s.lastIndexOf(\"d\") == -1;");
	}
}