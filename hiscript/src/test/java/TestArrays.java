import org.junit.jupiter.api.Test;

public class TestArrays extends HiTest {
	@Test
	public void testNew() {
		assertSuccessSerialize("int[] x1, x2; boolean y[]; String[] z1[] = {{}}, z2; assert z1 instanceof String[][];");
		assertSuccessSerialize("int[] x = {1,2}; boolean y[] = {true, false}; String[] z[] = {{\"a\", \"b\"}, new String[]{\"c\", \"d\"}};");
		assertSuccessSerialize("int[] x = new int[]{1,2}; boolean y[] = new boolean[]{true,false}; String[] z[] = new String[][]{{\"a\", \"b\"},{\"c\", \"d\"}};");
		assertSuccessSerialize("int [] [] x = {new int[]{1, 2, new Integer(3)}};");
		assertSuccessSerialize("class A{} class B extends A{} A[] x = new A[]{new A(), new B(), null}; assert x[1] instanceof B; assert x[2] == null; x[2] = new B(); assert x[2] instanceof B;");
		assertSuccessSerialize("Object x[] = {1, \"\"};");
		assertFailCompile("int x[] = {1, true};");
		assertFailCompile("byte x[] = {128};");
		assertFailCompile("short[] x = {" + (Short.MAX_VALUE + 1) + "};");
		assertFailCompile("char[] x = {-1};");
		assertFailCompile("class A{} class B extends A{} B[] x = new A[]{new B()};");
		assertFailCompile("class A{} class B extends A{} B[] x = new B[]{new A()};");
		assertFailCompile("int[][][] x = new int[1][][1];");
		assertFailCompile("int[][][] x = new int[][][];");
		assertFailCompile("int[][][] x = new int[1][1];");
		assertFailCompile("int[][][] x = new int[1][];");
		assertFailCompile("int[][][] x = new int[1];");
		assertFailCompile("int[][][] x = new int[];");
	}

	@Test
	public void testSetValue() {
		// boolean
		assertSuccessSerialize("boolean[] x = new boolean[1]; x[0] = true; assert x[0];");
		assertFailCompile("boolean[] x = new boolean[1]; x[0] = 1;");
		assertSuccessSerialize("boolean[] x = new boolean[1]; x[0] |= true; assert x[0];");

		// byte
		assertSuccessSerialize("byte[] x = new byte[1]; x[0] = new Byte((byte)1); assert x[0] == 1;");
		assertSuccessSerialize("byte[] x = new byte[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccessSerialize("byte[] x = new byte[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccessSerialize("byte[] x = new byte[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("byte[] x = new byte[1]; x[0] = \"\";");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1L;");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1F;");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1D;");
		assertSuccessSerialize("byte[] x = new byte[1]; x[0] += 1; assert x[0] == 1;");

		// short
		assertSuccessSerialize("short[] x = new short[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccessSerialize("short[] x = new short[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccessSerialize("short[] x = new short[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccessSerialize("short[] x = new short[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("short[] x = new short[1]; x[0] = \"\";");
		assertFailCompile("short[] x = new short[1]; x[0] = 1L;");
		assertFailCompile("short[] x = new short[1]; x[0] = 1F;");
		assertFailCompile("short[] x = new short[1]; x[0] = 1D;");
		assertSuccessSerialize("short[] x = new short[1]; x[0] += 1; assert x[0] == 1;");

		// char
		assertSuccessSerialize("char[] x = new char[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccessSerialize("char[] x = new char[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccessSerialize("char[] x = new char[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccessSerialize("char[] x = new char[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("char[] x = new char[1]; x[0] = \"\";");
		assertFailCompile("char[] x = new char[1]; x[0] = 1L;");
		assertFailCompile("char[] x = new char[1]; x[0] = 1F;");
		assertFailCompile("char[] x = new char[1]; x[0] = 1D;");
		assertSuccessSerialize("char[] x = new char[1]; x[0] += 1; assert x[0] == 1;");

		// int
		assertSuccessSerialize("int[] x = new int[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = new int[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = new int[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = new int[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("int[] x = new int[1]; x[0] = \"\";");
		assertFailCompile("int[] x = new int[1]; x[0] = 1L;");
		assertFailCompile("int[] x = new int[1]; x[0] = 1F;");
		assertFailCompile("int[] x = new int[1]; x[0] = 1D;");
		assertSuccessSerialize("int[] x = new int[1]; x[0] += 1; assert x[0] == 1;");

		// long
		assertSuccessSerialize("long[] x = new long[1]; x[0] = (byte)1; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = new long[1]; x[0] = (short)1; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = new long[1]; x[0] = (char)1; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = new long[1]; x[0] = (int)1; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = new long[1]; x[0] = (long)1; assert x[0] == 1L;");
		assertFailCompile("long[] x = new long[1]; x[0] = \"\";");
		assertFailCompile("long[] x = new long[1]; x[0] = 1F;");
		assertFailCompile("long[] x = new long[1]; x[0] = 1D;");
		assertSuccessSerialize("long[] x = new long[1]; x[0] += 1; assert x[0] == 1;");

		// float
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (byte)1; assert x[0] == 1F;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (short)1; assert x[0] == 1F;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (char)1; assert x[0] == 1F;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (int)1; assert x[0] == 1F;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (long)1; assert x[0] == 1F;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] = (float)1; assert x[0] == 1F;");
		assertFailCompile("float[] x = new float[1]; x[0] = \"\";");
		assertFailCompile("float[] x = new float[1]; x[0] = 1D;");
		assertSuccessSerialize("float[] x = new float[1]; x[0] += 1; assert x[0] == 1;");

		// double
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (byte)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (short)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (char)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (int)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (long)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (float)1; assert x[0] == 1D;");
		assertSuccessSerialize("double[] x = new double[1]; x[0] = (double)1; assert x[0] == 1D;");
		assertFailCompile("double[] x = new double[1]; x[0] = \"\";");
		assertSuccessSerialize("double[] x = new double[1]; x[0] += 1; assert x[0] == 1;");
	}

	@Test
	public void testCast() {
		assertSuccessSerialize("int[] x = {}; int[] y = x; assert x == y;");
		assertSuccessSerialize("int[] x = {}; int[] y = (int[])x; assert x == y;");
		assertFailCompile("byte[] x = {}; int[] y = x;");
		assertFailCompile("byte[] x = {}; int[] y = (int[])x;");
		assertSuccessSerialize("class A{} class B extends A{} B[] x = {new B()}; A[] y = x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		assertSuccessSerialize("class A{} class B extends A{} A[] x = new B[]{new B()}; B[] y = (B[])x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		assertSuccessSerialize("int[] x = {}; int[] y = (int[])x;");
		assertSuccessSerialize("String[] x = {\"a\", \"b\"}; assert x instanceof String[];");
		assertSuccessSerialize("int[][] x = {{1}, {2}}; assert x instanceof int[][];");
		assertSuccessSerialize("class A{int get(int x){return x + 1;}} A[][][] a = {null, {null, new A[]{null, new A()}}}; assert a[1][1][1].get(1) == 2;");
		assertSuccessSerialize("interface A{int get(int x);} A[][] a = {{x->x+1}}; assert a[0][0].get(1) == 2;");

		assertSuccessSerialize("int[] x = {1}; assert x[(byte)0] == 1;");
		assertSuccessSerialize("int[] x = {1}; assert x[(short)0] == 1;");
		assertSuccessSerialize("int[] x = new int['b']; assert x['a'] == 0;");
		assertFailCompile("int[] x = {1}; assert x[0L] == 1;");
		assertFailCompile("int[] x = {1}; assert x[0D] == 1;");
		assertFailCompile("int[] x = {1}; assert x[0F] == 1;");

		// autocast
		assertSuccessSerialize("byte[] x = {(short)1}; assert x[0] == 1;");
		assertFailCompile("byte[] x = {(short)128};");
		assertSuccessSerialize("byte[] x = {'a'}; assert x[0] == 'a';");
		assertSuccessSerialize("byte[] x = {(int)1}; assert x[0] == 1;");
		assertFailCompile("byte[] x = {(int)128};");
		assertFailCompile("byte[] x = {1L};");
		assertFailCompile("byte[] x = {1F};");
		assertFailCompile("byte[] x = {1D};");

		assertSuccessSerialize("short[] x = {(byte)1}; assert x[0] == 1;");
		assertSuccessSerialize("short[] x = {'a'}; assert x[0] == 'a';");
		assertSuccessSerialize("short[] x = {(int)1}; assert x[0] == 1;");
		assertFailCompile("short[] x = {" + (Short.MAX_VALUE + 1) + "};");
		assertFailCompile("short[] x = {1L};");
		assertFailCompile("short[] x = {1F};");
		assertFailCompile("short[] x = {1D};");

		assertSuccessSerialize("int[] x = {(byte)1}; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = {'a'}; assert x[0] == 'a';");
		assertSuccessSerialize("int[] x = {(short)1}; assert x[0] == 1;");
		assertFailCompile("int[] x = {1L};");
		assertFailCompile("int[] x = {1F};");
		assertFailCompile("int[] x = {1D};");

		assertSuccessSerialize("long[] x = {(byte)1}; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = {(short)1}; assert x[0] == 1L;");
		assertSuccessSerialize("long[] x = {'a'}; assert x[0] == 'a';");
		assertFailCompile("long[] x = {1F};");
		assertFailCompile("long[] x = {1D};");
	}

	@Test
	public void testExpressions() {
		assertSuccessSerialize("int[] x = {0, 1, 2}; assert x.length == 3; assert x[0] == 0; assert x[1] == 1; assert x[2] == 2; assert x[x[x[x[x[2]-1]]]] == 1;");
		assertSuccessSerialize("int[][] x = {{0, 1, 2}, {3, 5, 6}, {7, 8, 9}}; int y = x[0][0] + x[2][2]; assert y == 9;");
		assertSuccessSerialize("String[][] s = {null}; assert !(s[0] instanceof String[]);");
		assertSuccessSerialize("int[][] x = {null}; assert !(x[0] instanceof int[]);");
		assertSuccessSerialize("String[] x = {null}; assert x[0] == null;");
		assertSuccessSerialize("String[][] x = {null}; assert x[0] == null;");
		assertSuccessSerialize("int[][][][][][][][][][] x = {null, {null, {null, {null, {null, {null, {null, {null, {null, { 1 }}}}}}}}}}; assert x[1] != null;");

		assertSuccessSerialize("int[] x = new int[]{0}; x[0]++; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = new int[]{1}; x[0] = 0; assert x[0] == 0;");
		assertSuccessSerialize("String[] x = {null}; x[0] = \"\"; x[0] += 0.000; x[0] += 3L; assert x[0].equals(\"0.03\");");
	}

	@Test
	public void testStatements() {
		assertSuccessSerialize("int[] x = {1, 1, 1, 1}; int c = 0; for (int i : x) c++; assert c == x.length;");
	}
}