import org.junit.jupiter.api.Test;

public class TestJava extends HiTest {
	public static class B {
		int x;

		public B(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}
	}

	@Test
	public void test() {
		assertSuccess("interface JString{int length(); int indexOf(int ch); String toUpperCase();} " + //
				"JString x = (JString)Java.newInstance(JString.class, \"java.lang.String\", \"abc\"); " + //
				"assert x.length() == 3; " + //
				"assert x.indexOf('b') == 1; " + //
				"assert x.toUpperCase().equals(\"ABC\");");
		assertSuccess("interface B{int getX();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.getX() == 1;");
	}
}