import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestString extends HiTest {
	@Test
	public void test() throws IOException {
		assertSuccessSerialize("String s; s = \"a\"; assert s.length() == 1; assert s.equals(\"a\");");
		assertSuccessSerialize("String s = \"\"; assert s.length() == 0; assert s.equals(\"\");");
		assertSuccessSerialize("String s = null; assert s == null;");
		assertSuccessSerialize("String s = \"\"; s += \"A\"; assert s.equals(\"A\");");
		assertSuccessSerialize("String s = null; s += \"A\"; assert s.equals(\"nullA\");");

		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf('s') == 2;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf(\"string\") == 5;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf('s', 4) == 5;");
		assertSuccessSerialize("String s = \"test string\"; assert s.indexOf(\"string\", 5) == 5;");

		assertSuccessSerialize("String s = \"test test\"; assert s.lastIndexOf('e') == 6;");
		assertSuccessSerialize("String s = \"test test\"; assert s.lastIndexOf(\"test\") == 5;");

		assertSuccessSerialize("char[] chars = \"test string\".toCharArray(); assert chars.length == 11;");
		assertSuccessSerialize("byte[] bytes = \"abc\".getBytes(); assert bytes.length == 3;");

		assertSuccessSerialize("assert \"abcd\".substring(1, 3).equals(\"bc\");");
		assertSuccessSerialize("assert \"abcd\".substring(2).equals(\"cd\");");
		assertSuccessSerialize("assert \" abc \".trim().equals(\"abc\");");
		assertSuccessSerialize("assert \"abc\".toUpperCase().equals(\"ABC\");");
		assertSuccessSerialize("assert \"ABC\".toLowerCase().equals(\"abc\");");
	}
}