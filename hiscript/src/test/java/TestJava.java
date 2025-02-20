import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJava extends HiTest {
	public static class B {
		int x;

		public B(int x) {
			this.x = x;
		}

		public int getX() {
			return x;
		}

		public String getString() {
			return "abc";
		}

		public String getString(String s) {
			assert s.equals("abc");
			return s + "d";
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

		public Object getNull(Object o) {
			assert o == null;
			return o;
		}

		public HashMap getHashMap(HashMap x) {
			assert x.get("number").equals(123);
			x.put("k", "v");
			x.put("array", new Byte[] {1, 2, 3, null});
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

			Map m = new HashMap();
			m.put("a", 1);
			m.put(1.0, true);
			x.add(m);
			x.add(new Map[] {Map.of("key", "value")});

			List l = new ArrayList();
			l.add("a");
			l.add(1);
			l.add(true);
			x.add(l);
			x.add(new List[] {List.of(1, 2, 3)});

			x.add(new Byte[] {1, 2, 3, null});
			x.add(new Short[] {1, 2, 3, null});
			x.add(new Character[] {1, 2, 3, null});
			x.add(new Integer[] {1, 2, 3, null});
			x.add(new Long[] {1l, 2l, 3l, null});
			x.add(new Float[] {1f, 2f, 3f, null});
			x.add(new Double[] {1d, 2d, 3d, null});
			x.add(new Boolean[] {true, false, null});
			return x;
		}

		byte[][] byteArray;

		public void setByteArray(byte[][] byteArray) {
			this.byteArray = byteArray;
		}

		public byte[][] getByteArray() {
			return byteArray;
		}

		short[] shortArray;

		public void setShortArray(short[] shortArray) {
			this.shortArray = shortArray;
		}

		public short[] getShortArray() {
			return shortArray;
		}

		public char[] getCharArray(char[] array) {
			return array;
		}

		public int[] getIntArray(int[] array) {
			return array;
		}

		public int[][] getIntArray2() {
			return new int[][] {{x}};
		}

		public long[] getLongArray(long[] array) {
			return array;
		}

		public float[] getFloatArray(float[] array) {
			return array;
		}

		public double[] getDoubleArray(double[] array) {
			return array;
		}

		public boolean[] getBooleanArray(boolean[] array) {
			return array;
		}

		public String[] getStringArray(String[] array) {
			return array;
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
		assertSuccess("interface B{String getString();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert \"abc\".equals(b.getString());");
		assertSuccess("interface B{String getString(String s);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); String s = \"abc\"; s = b.getString(s); assert \"abcd\".equals(s);");
		assertSuccess("interface B{HashMap getHashMap(HashMap m);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); HashMap m = new HashMap(); m.put(\"number\", 123); m = b.getHashMap(m); assert \"v\".equals(m.get(\"k\")); assert new Integer(123).equals(m.get(\"number\")); assert m.get(\"array\") instanceof Byte[];");
		assertSuccess("interface B{ArrayList getArrayList(ArrayList m);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); ArrayList a = new ArrayList(); a.add(\"e1\"); a.add(null); a = b.getArrayList(a); assert \"e1\".equals(a.get(0)); assert a.get(1) == null; assert \"e2\".equals(a.get(2)); assert a.get(3) == null;");
		assertSuccess("interface B{void setByteArray(byte[][] arr); byte[][] getByteArray();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); b.setByteArray(new byte[][]{{}, {1,2,3,4,5}}); byte[][] x = b.getByteArray(); assert x.length == 2 && x[1].length == 5 && x[1][4] == 5;");
		assertSuccess("interface B{void setShortArray(short[] arr); short[] getShortArray();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); b.setShortArray(new short[]{1,2,3}); short[] x = b.getShortArray(); assert x[2] == 3;");
		assertSuccess("interface B{char[] getCharArray(char[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); char[] x = b.getCharArray(new char[]{'x'}); assert x[0] == 'x';");
		assertSuccess("interface B{int[] getIntArray(int[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); int[] x = b.getIntArray(new int[]{1}); assert x[0] == 1;");
		assertSuccess("interface B{int[][] getIntArray2();} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); int[][] x = b.getIntArray2(); assert x[0][0] == 1;");
		assertSuccess("interface B{long[] getLongArray(long[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); long[] x = b.getLongArray(new long[]{1}); assert x[0] == 1;");
		assertSuccess("interface B{float[] getFloatArray(float[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); float[] x = b.getFloatArray(new float[]{1f}); assert x[0] == 1;");
		assertSuccess("interface B{double[] getDoubleArray(double[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); double[] x = b.getDoubleArray(new double[]{1.0}); assert x[0] == 1;");
		assertSuccess("interface B{boolean[] getBooleanArray(boolean[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); boolean[] x = b.getBooleanArray(new boolean[]{true}); assert x[0];");
		assertSuccess("interface B{String[] getStringArray(String[] arr);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); String[] x = b.getStringArray(new String[]{\"abc\"}); assert x[0].equals(\"abc\");");
		assertSuccess("interface B{Object getNull(Object o);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.getNull(null) == null;");

		// primitive types
		String[] primitiveNumberTypes = {"byte", "short", "char", "int", "long", "float", "double"};
		String[] primitiveNumberBoxedTypes = {"Byte", "Short", "Character", "Integer", "Long", "Float", "Double"};
		for (int i = 0; i < primitiveNumberTypes.length; i++) {
			String t = primitiveNumberTypes[i];
			String T = primitiveNumberBoxedTypes[i];
			assertSuccess("interface B{" + t + " get" + T + "(" + t + " x);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.get" + T + "((" + t + ")127) == 127;");
		}
		assertSuccess("interface B{boolean getBoolean(boolean x);} B b = (B)Java.newInstance(B.class, \"TestJava$B\", 1); assert b.getBoolean(true);");
	}
}