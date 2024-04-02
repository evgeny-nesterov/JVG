import org.junit.jupiter.api.Test;

public class TestJava extends HiTest {
	@Test
	public void test() {
		assertSuccess("interface JString{int length(); int indexOf(int ch); String toUpperCase();} " + //
				"JString x = (JString)Java.newInstance(JString.class, \"java.lang.String\", \"abc\"); " + //
				"assert x.length() == 3; " + //
				"assert x.indexOf('b') == 1; " + //
				"assert x.toUpperCase().equals(\"ABC\");");
	}
}