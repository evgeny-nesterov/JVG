import org.junit.jupiter.api.Test;

import java.util.HashMap;

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

		public byte getByte(byte x) {
			return x;
		}

		public short getShort(short x) {
			return x;
		}

		public char getCharacter(char x) {
			return x;
		}

		public int getInteger(int x) {
			return x;
		}

		public long getLong(long x) {
			return x;
		}

		public float getFloat(float x) {
			return x;
		}

		public double getDouble(double x) {
			return x;
		}

		public boolean getBoolean(boolean x) {
			return x;
		}

		public HashMap getHashMap(HashMap x) {
			x.put("k", "v");
			return x;
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
		assertSuccessSerialize("interface B{String getText();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert \"abc\".equals(b.getText());");
		assertSuccessSerialize("interface B{String getText();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert \"abc\".equals(b.getText());");
		assertSuccessSerialize("interface B{HashMap getHashMap(HashMap m);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); HashMap m = b.getHashMap(new HashMap()); assert \"v\".equals(m.get(\"k\"));");

		// primitive types
		String[] primitiveNumberTypes = {"byte", "short", "char", "int", "long", "float", "double"};
		String[] primitiveNumberBoxedTypes = {"Byte", "Short", "Character", "Integer", "Long", "Float", "Double"};
		for (int i = 0; i < primitiveNumberTypes.length; i++) {
			String t = primitiveNumberTypes[i];
			String T = primitiveNumberBoxedTypes[i];
			assertSuccessSerialize("interface B{" + t + " get" + T + "(" + t + " x);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.get" + T + "((" + t + ")127) == 127;");
		}
		assertSuccessSerialize("interface B{boolean getBoolean(boolean x);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.getBoolean(true);");
	}
}