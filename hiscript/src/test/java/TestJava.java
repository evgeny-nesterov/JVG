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

		public String getText() {
			return "abc";
		}
	}

	@Test
	public void test() {
		assertSuccessSerialize("interface JString{int length(); int indexOf(int ch); String toUpperCase();} " + //
				"JString x = (JString)Java.newInstance(JString.class, \"java.lang.String\", \"abc\"); " + //
				"assert x.length() == 3; " + //
				"assert x.indexOf('b') == 1; " + //
				"assert x.toUpperCase().equals(\"ABC\");");
		assertSuccessSerialize("interface B{int getX();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.getX() == 1;");

		// TODO fix B duplicate
		assertSuccessSerialize("interface B{String getText();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert \"abc\".equals(b.getText());");
	}
}