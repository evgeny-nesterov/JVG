import org.junit.jupiter.api.Test;

public class TestArrays extends HiTest {
	@Test
	public void testNew() {
		assertSuccess("int[] x1, x2; boolean y[]; String[] z1[] = {{}}, z2; assert z1 instanceof String[][];");
		assertSuccess("int[] x = {1,2}; boolean y[] = {true, false}; String[] z[] = {{\"a\", \"b\"}, new String[]{\"c\", \"d\"}};");
		assertSuccess("int[] x = new int[]{1,2}; boolean y[] = new boolean[]{true,false}; String[] z[] = new String[][]{{\"a\", \"b\"},{\"c\", \"d\"}};");
		assertSuccess("int [] [] x = {new int[]{1, 2, new Integer(3)}};");
		assertSuccess("class A{} class B extends A{} A[] x = new A[]{new A(), new B(), null}; assert x[1] instanceof B; assert x[2] == null; x[2] = new B(); assert x[2] instanceof B;");

		assertFailCompile("int x[] = {1, true};", //
				"int is expected");
		assertFailCompile("byte x[] = {128};", //
				"byte is expected");
		assertFailCompile("short[] x = {" + (Short.MAX_VALUE + 1) + "};", //
				"short is expected");
		assertFailCompile("char[] x = {-1};", //
				"char is expected");
		assertFailCompile("class A{} class B extends A{} B[] x = new A[]{new B()};", //
				"incompatible types: A[] cannot be converted to B[]");
		assertFailCompile("class A{} class B extends A{} B[] x = new B[]{new A()};", //
				"B is expected");

		assertFailCompile("int[][][] x = new int[1][][1];", //
				"invalid array size value");
		assertFailCompile("int[][][] x = new int[][][];", //
				"dimension is expected");
		assertFailCompile("int[][][] x = new int[1][1];", //
				"incompatible types: int[][] cannot be converted to int[][][]");
		assertFailCompile("int[][][] x = new int[1][];", //
				"incompatible types: int[][] cannot be converted to int[][][]");
		assertFailCompile("int[][][] x = new int[1];", //
				"incompatible types: int[] cannot be converted to int[][][]");
		assertFailCompile("int[][][] x = new int[];", //
				"dimension is expected");
		assertFailCompile("int x = new int[0];", //
				"incompatible types: int[] cannot be converted to int");
		assertFailCompile("int x = {};", //
				"array initializer is not allowed here");
		assertFailCompile("int[] x = {{}};", //
				"array initializer is not allowed here");
		assertFailCompile("int[] x = {1, {2}};", //
				"array initializer is not allowed here");
		assertFailCompile("int[][] x = {{}, {{}}};", //
				"array initializer is not allowed here");
		assertFailCompile("int[] x = {true};", //
				"int is expected");
		assertFailCompile("String[] x = {1};", //
				"String is expected");
		assertFail("int[] x = {1}; int y = x[-1];",
				"array index out of bound: array length = 1, index = -1");

		// Object
		assertSuccess("Object[] x = {1, \"\", true, 1L, 1f, 1d, new Object(), new Integer(1), new boolean[][]{{true}}, new Object[0], null, new String[]{\"abc\"}};");
		assertSuccess("Object[] x = new Object[1][1];");
		assertSuccess("Object[] x = new Object[1][1][][];");
		assertSuccess("Object[] x = new Object[][]{};");
		assertSuccess("Object[] x = new Object[][]{{}};");
		assertSuccess("Object[][] x = {null, new Object[1], new Object[1][1], {}, {1, 2, 3}};");
		assertSuccess("Object[] x = null;");
		assertSuccess("Object[] x = {null};");
		assertSuccess("Object[][] x = {null};");
		assertSuccess("Object[][][] x = {null};");
		assertSuccess("Object[][][] x = {{null, null}};");

		assertFailCompile("Object[] x = {{}};", //
				"array initializer is not allowed here");
		assertFailCompile("Object[] x = new Object[1]{null};", //
				"';' is expected");
		assertFailCompile("Object[] x = new Object[1]{null};", //
				"';' is expected");
		assertFailCompile("Object[] x = new Object[][]{{{}}};", //
				"array initializer is not allowed here");
		assertFailCompile("Object[] x = 1;", //
				"incompatible types: int cannot be converted to Object");
		assertFailCompile("Object[] x = new Object();", //
				"incompatible types: Object cannot be converted to Object[]");
		assertFailCompile("Object[][] x = {1};", //
				"Object[] is expected");
		assertFailCompile("Object[][][] x = {{1}};", //
				"Object[] is expected");
	}

	@Test
	public void testSetValue() {
		// boolean
		assertSuccess("boolean[] x = new boolean[1]; x[0] = true; assert x[0];");
		assertFailCompile("boolean[] x = new boolean[1]; x[0] = 1;", //
				"operator '=' can not be applied to boolean, int");
		assertSuccess("boolean[] x = new boolean[1]; x[0] |= true; assert x[0];");

		// byte
		assertSuccess("byte[] x = new byte[1]; x[0] = new Byte((byte)1); assert x[0] == 1;");
		assertSuccess("byte[] x = new byte[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccess("byte[] x = new byte[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccess("byte[] x = new byte[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("byte[] x = new byte[1]; x[0] = \"\";", //
				"operator '=' can not be applied to byte, String");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1L;", //
				"operator '=' can not be applied to byte, long");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1F;", //
				"operator '=' can not be applied to byte, float");
		assertFailCompile("byte[] x = new byte[1]; x[0] = 1D;", //
				"operator '=' can not be applied to byte, double");
		assertSuccess("byte[] x = new byte[1]; x[0] += 1; assert x[0] == 1;");

		// short
		assertSuccess("short[] x = new short[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccess("short[] x = new short[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccess("short[] x = new short[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccess("short[] x = new short[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("short[] x = new short[1]; x[0] = \"\";", //
				"operator '=' can not be applied to short, String");
		assertFailCompile("short[] x = new short[1]; x[0] = 1L;", //
				"operator '=' can not be applied to short, long");
		assertFailCompile("short[] x = new short[1]; x[0] = 1F;", //
				"operator '=' can not be applied to short, float");
		assertFailCompile("short[] x = new short[1]; x[0] = 1D;", //
				"operator '=' can not be applied to short, double");
		assertSuccess("short[] x = new short[1]; x[0] += 1; assert x[0] == 1;");

		// char
		assertSuccess("char[] x = new char[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccess("char[] x = new char[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccess("char[] x = new char[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccess("char[] x = new char[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("char[] x = new char[1]; x[0] = \"\";", //
				"operator '=' can not be applied to char, String");
		assertFailCompile("char[] x = new char[1]; x[0] = 1L;", //
				"operator '=' can not be applied to char, long");
		assertFailCompile("char[] x = new char[1]; x[0] = 1F;", //
				"operator '=' can not be applied to char, float");
		assertFailCompile("char[] x = new char[1]; x[0] = 1D;", //
				"operator '=' can not be applied to char, double");
		assertSuccess("char[] x = new char[1]; x[0] += 1; assert x[0] == 1;");

		// int
		assertSuccess("int[] x = new int[1]; x[0] = (byte)1; assert x[0] == 1;");
		assertSuccess("int[] x = new int[1]; x[0] = (short)1; assert x[0] == 1;");
		assertSuccess("int[] x = new int[1]; x[0] = (char)1; assert x[0] == 1;");
		assertSuccess("int[] x = new int[1]; x[0] = (int)1; assert x[0] == 1;");
		assertFailCompile("int[] x = new int[1]; x[0] = \"\";", //
				"operator '=' can not be applied to int, String");
		assertFailCompile("int[] x = new int[1]; x[0] = 1L;", //
				"operator '=' can not be applied to int, long");
		assertFailCompile("int[] x = new int[1]; x[0] = 1F;", //
				"operator '=' can not be applied to int, float");
		assertFailCompile("int[] x = new int[1]; x[0] = 1D;", //
				"operator '=' can not be applied to int, double");
		assertSuccess("int[] x = new int[1]; x[0] += 1; assert x[0] == 1;");

		// long
		assertSuccess("long[] x = new long[1]; x[0] = (byte)1; assert x[0] == 1L;");
		assertSuccess("long[] x = new long[1]; x[0] = (short)1; assert x[0] == 1L;");
		assertSuccess("long[] x = new long[1]; x[0] = (char)1; assert x[0] == 1L;");
		assertSuccess("long[] x = new long[1]; x[0] = (int)1; assert x[0] == 1L;");
		assertSuccess("long[] x = new long[1]; x[0] = (long)1; assert x[0] == 1L;");
		assertFailCompile("long[] x = new long[1]; x[0] = \"\";", //
				"operator '=' can not be applied to long, String");
		assertFailCompile("long[] x = new long[1]; x[0] = 1F;", //
				"operator '=' can not be applied to long, float");
		assertFailCompile("long[] x = new long[1]; x[0] = 1D;", //
				"operator '=' can not be applied to long, double");
		assertSuccess("long[] x = new long[1]; x[0] += 1; assert x[0] == 1;");

		// float
		assertSuccess("float[] x = new float[1]; x[0] = (byte)1; assert x[0] == 1F;");
		assertSuccess("float[] x = new float[1]; x[0] = (short)1; assert x[0] == 1F;");
		assertSuccess("float[] x = new float[1]; x[0] = (char)1; assert x[0] == 1F;");
		assertSuccess("float[] x = new float[1]; x[0] = (int)1; assert x[0] == 1F;");
		assertSuccess("float[] x = new float[1]; x[0] = (long)1; assert x[0] == 1F;");
		assertSuccess("float[] x = new float[1]; x[0] = (float)1; assert x[0] == 1F;");
		assertFailCompile("float[] x = new float[1]; x[0] = \"\";", //
				"operator '=' can not be applied to float, String");
		assertFailCompile("float[] x = new float[1]; x[0] = 1D;", //
				"operator '=' can not be applied to float, double");
		assertSuccess("float[] x = new float[1]; x[0] += 1; assert x[0] == 1;");

		// double
		assertSuccess("double[] x = new double[1]; x[0] = (byte)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (short)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (char)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (int)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (long)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (float)1; assert x[0] == 1D;");
		assertSuccess("double[] x = new double[1]; x[0] = (double)1; assert x[0] == 1D;");
		assertFailCompile("double[] x = new double[1]; x[0] = \"\";", //
				"operator '=' can not be applied to double, String");
		assertSuccess("double[] x = new double[1]; x[0] += 1; assert x[0] == 1;");

		// Object
		assertSuccess("Object[] a = new Object[1]; a[0] = \"text\"; assert a[0].equals(\"text\");");
		assertSuccess("Object[][] a = new Object[1][]; a[0] = new Object[]{1L}; assert a[0][0].equals(new Long(1L));");
	}

	@Test
	public void testCast() {
		assertSuccess("Object x = new int[1]; int[] y = (int[])x; assert y instanceof int[]; assert y == x;");
		assertFailCompile("Object x = new int[1]; long[] y = (int[])x;", //
				"incompatible types: int[] cannot be converted to long[]");

		assertSuccess("int[] x = {}; int[] y = x; assert x == y;");
		assertSuccess("int[] x = {}; int[] y = (int[])x; assert x == y;");
		assertFailCompile("byte[] x = {}; int[] y = x;", //
				"incompatible types: byte[] cannot be converted to int[]");
		assertFailCompile("byte[] x = {}; int[] y = (int[])x;", //
				"inconvertible types; cannot cast 'byte[]' to 'int[]'");
		assertSuccess("class A{} class B extends A{} B[] x = {new B()}; A[] y = x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		assertSuccess("class A{} class B extends A{} A[] x = new B[]{new B()}; B[] y = (B[])x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		assertSuccess("int[] x = {}; int[] y = (int[])x;");
		assertSuccess("String[] x = {\"a\", \"b\"}; assert x instanceof String[];");
		assertSuccess("int[][] x = {{1}, {2}}; assert x instanceof int[][];");
		assertSuccess("class A{int get(int x){return x + 1;}} A[][][] a = {null, {null, new A[]{null, new A()}}}; assert a[1][1][1].get(1) == 2;");
		assertSuccess("interface A{int get(int x);} A[][] a = {{x->x+1}}; assert a[0][0].get(1) == 2;");

		assertSuccess("int[] x = {1}; assert x[(byte)0] == 1;");
		assertSuccess("int[] x = {1}; assert x[(short)0] == 1;");
		assertSuccess("int[] x = new int['b']; assert x['a'] == 0;");
		assertFailCompile("int[] x = {1}; assert x[0L] == 1;", //
				"cannot cast long to int");
		assertFailCompile("int[] x = {1}; assert x[0D] == 1;", //
				"cannot cast double to int");
		assertFailCompile("int[] x = {1}; assert x[0F] == 1;", //
				"cannot cast float to int");

		// autocast
		assertSuccess("byte[] x = {(short)1}; assert x[0] == 1;");
		assertFailCompile("byte[] x = {(short)128};", //
				"byte is expected");
		assertSuccess("byte[] x = {'a'}; assert x[0] == 'a';");
		assertSuccess("byte[] x = {(int)1}; assert x[0] == 1;");
		assertFailCompile("byte[] x = {(int)128};", //
				"byte is expected");
		assertFailCompile("byte[] x = {1L};", //
				"byte is expected");
		assertFailCompile("byte[] x = {1F};", //
				"byte is expected");
		assertFailCompile("byte[] x = {1D};", //
				"byte is expected");

		assertSuccess("short[] x = {(byte)1}; assert x[0] == 1;");
		assertSuccess("short[] x = {'a'}; assert x[0] == 'a';");
		assertSuccess("short[] x = {(int)1}; assert x[0] == 1;");
		assertFailCompile("short[] x = {" + (Short.MAX_VALUE + 1) + "};", //
				"short is expected");
		assertFailCompile("short[] x = {1L};", //
				"short is expected");
		assertFailCompile("short[] x = {1F};", //
				"short is expected");
		assertFailCompile("short[] x = {1D};", //
				"short is expected");

		assertSuccess("int[] x = {(byte)1}; assert x[0] == 1;");
		assertSuccess("int[] x = {'a'}; assert x[0] == 'a';");
		assertSuccess("int[] x = {(short)1}; assert x[0] == 1;");
		assertFailCompile("int[] x = {1L};", //
				"int is expected");
		assertFailCompile("int[] x = {1F};", //
				"int is expected");
		assertFailCompile("int[] x = {1D};", //
				"int is expected");

		assertSuccess("long[] x = {(byte)1}; assert x[0] == 1L;");
		assertSuccess("long[] x = {(short)1}; assert x[0] == 1L;");
		assertSuccess("long[] x = {'a'}; assert x[0] == 'a';");
		assertFailCompile("long[] x = {1F};", //
				"long is expected");
		assertFailCompile("long[] x = {1D};", //
				"long is expected");

		assertFail("Object a = new Integer[0]; String[] s = (String[])a;", //
				"cannot cast Integer[] to String[]");
		assertFail("Object a = new int[0]; double[] b = (double[])a;", //
				"cannot cast int[] to double[]");
		assertFail("Object a = 1; double[] b = (double[])a;", //
				"cannot cast int to double[]");
		assertFail("Object a = new int[0]; Double b = (Double)a;", //
				"cannot cast int[] to Double");

		assertSuccess("int[] x = {}; Object o = (Object)x;");
		assertFailCompile("int[] x = {}; int[][] o = (int[][])x;", //
				"inconvertible types; cannot cast 'int[]' to 'int[][]'");
		assertFail("Object x = new int[]{}; int[][] o = (int[][])x;", //
				"cannot cast int[] to int[][]");
		assertFailCompile("int[][] x = {{}}; int[] o = (int[])x;", //
				"inconvertible types; cannot cast 'int[][]' to 'int[]'");
		assertFail("Object x = new int[][]{{}}; int[] o = (int[])x;", //
				"cannot cast int[][] to int[]");
		assertFail("Object x = new Object[]{}; int[] o = (int[])x;", //
				"cannot cast Object[] to int[]");
		assertFail("Object x = new int[]{}; Object[] o = (Object[])x;", //
				"cannot cast int[] to Object[]");
	}

	@Test
	public void testExpressions() {
		assertSuccess("int[] x = {0, 1, 2}; assert x.length == 3; assert x[0] == 0; assert x[1] == 1; assert x[2] == 2; assert x[x[x[x[x[2]-1]]]] == 1;");
		assertSuccess("int[][] x = {{0, 1, 2}, {3, 5, 6}, {7, 8, 9}}; int y = x[0][0] + x[2][2]; assert y == 9;");
		assertSuccess("String[][] s = {null}; assert !(s[0] instanceof String[]);");
		assertSuccess("int[][] x = {null}; assert x[0] == null; assert !(x[0] instanceof int[]);");
		assertSuccess("String[] x = {null}; assert x[0] == null; assert !(x[0] instanceof String);");
		assertSuccess("String[][] x = {null}; assert x[0] == null;");
		assertSuccess("int[][][][][][][][][][] x = {null, {null, {null, {null, {null, {null, {null, {null, {null, { 1 }}}}}}}}}}; assert x[1] != null; assert x[1][1][1][1][1][1][1][1][1][0] == 1;");
		assertFail("String[][] x = {null}; String s = x[0][0]", //
				"null pointer");
		assertFail("int[] x = {1,2,3}; int i = x[3]", //
				"array index out of bound: array length = 3, index = 3");
		assertFail("int[][] x = {{1,2},{3,4}}; int i = x[1][2]", //
				"array index out of bound: array length = 2, index = 2");

		assertSuccess("int[] x = new int[]{0}; x[0]++; assert x[0] == 1;");
		assertSuccess("int[] x = new int[]{1}; x[0] = 0; assert x[0] == 0;");
		assertSuccess("String[] x = {null}; x[0] = \"\"; x[0] += 0.000; x[0] += 3L; assert x[0].equals(\"0.03\");");

		assertFail("int[] x = null; int length = x.length", //
				"null pointer");
		assertFailCompile("int x = *+-;", //
				"expression expected");
		assertFailCompile("int x = **1;", //
				"expression expected");
		assertFailCompile("int x = *%;", //
				"expression expected");
	}

	@Test
	public void testStatements() {
		assertSuccess("int[] x = {1, 1, 1, 1}; int c = 0; for(int i : x) c++; assert c == x.length;");
		assertSuccess("int[] x = {1, 1, 1, 1}; for(int i = 0; i < x.length; i++) x[i]++; for(int i = 0; i < 4; i++) assert x[i] == 2;");
		assertSuccess("int[][] x = {{1,2},{3,4}}; for(int i = 0; i < x.length; i++) for(int j = 0; j < x[i].length; j++) x[i][j]++; assert x[0][0] == 2; assert x[0][1] == 3; assert x[1][0] == 4; assert x[1][1] == 5;");
	}
}