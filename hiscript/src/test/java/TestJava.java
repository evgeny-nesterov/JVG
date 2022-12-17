import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestJava extends HiTest {
	@Test
	public void testSingle() throws IOException {
		assertSuccess("Java.importClass(\"JByte\", \"java.lang.Byte\"); assert new JByte(127).byteValue() == 127;");
		assertSuccess("Java.importClass(\"JInteger\", \"java.lang.Integer\"); assert new JInteger(1).intValue() == 1;");
		assertSuccess("Java.importClass(\"JCharacter\", \"java.lang.Character\"); assert new JCharacter('x').charValue() == 'x';");
		assertSuccess("Java.importClass(\"JString\", \"java.lang.String\"); assert new JString(\"abc\").charAt(1) == 'b';");
		assertSuccess("assert new JString(new char[]{'a', 'b', 'c'}, 1, 1).charAt(0) == 'b';");
		assertFail("Java.importClass(\"String\", \"java.lang.String\");");

		// fields
		assertSuccess("Java.importClass(\"JString\", \"java.lang.String\"); char[] chars = new JString(\"abc\").value; assert new String(chars).equals(\"abc\");");
	}
}