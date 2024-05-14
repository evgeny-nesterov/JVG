import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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
			assert x.get("number").equals(123);
			x.put("k", "v");
			return x;
		}

		public ArrayList getArrayList(ArrayList x) {
			assert x.get(0).equals("e1");
			x.add("e2");
			x.add(null);
			x.add((byte) 1);
			x.add((short) 2);
			x.add('x');
			x.add(3);
			x.add(4L);
			x.add(5F);
			x.add(6D);
			x.add(true);
			x.add(new byte[] {1, 2, 3});
			x.add(new short[] {1, 2, 3});
			x.add(new char[] {1, 2, 3});
			x.add(new int[] {1, 2, 3});
			x.add(new long[] {1, 2, 3});
			x.add(new float[] {1, 2, 3});
			x.add(new double[] {1, 2, 3});
			x.add(new int[][] {{1, 2, 3}, {4, 5, 6}});
			x.add(new String[] {"a", "b", "c"});
			x.add(new HashMap());
			x.add(new ArrayList());
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
		assertSuccessSerialize("interface B{HashMap getHashMap(HashMap m);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); HashMap m = new HashMap(); m.put(\"number\", 123); m = b.getHashMap(m); assert \"v\".equals(m.get(\"k\")); assert new Integer(123).equals(m.get(\"number\"));");
		assertSuccessSerialize("interface B{ArrayList getArrayList(ArrayList m);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); ArrayList a = new ArrayList(); a.add(\"e1\"); a.add(null); a = b.getArrayList(a); assert \"e1\".equals(a.get(0)); assert a.get(1) == null; assert \"e2\".equals(a.get(2)); assert a.get(3) == null;");

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