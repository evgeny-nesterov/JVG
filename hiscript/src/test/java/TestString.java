import org.junit.jupiter.api.Test;

public class TestString extends HiTest {
	@Test
	public void test() {
		// declaration
		assertSuccessSerialize("String s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccessSerialize("String s; s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccessSerialize("String s = \"\"; assert s.length() == 0; assert s.equals(\"\");");
		assertSuccessSerialize("String s = null; assert s == null;");
		assertSuccessSerialize("String s = new String(\"abc\"); assert s.equals(\"abc\");");
		assertSuccessSerialize("String s1 = \"abc\"; String s2 = new String(s1); assert s2.equals(\"abc\");");
		assertFailCompile("String s = 1;");
		assertFailCompile("String s = 'a';");
		assertFailCompile("String s = true;");

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
	}
}