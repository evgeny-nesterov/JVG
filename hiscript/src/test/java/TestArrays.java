import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestArrays extends HiTest {
	@Test
	public void testNew() throws IOException {
		assertSuccessSerialize("int[] x; boolean y[]; String[] z[];");
		assertSuccessSerialize("int[] x = {1,2}; boolean y[] = {true,false}; String[] z[] = {{\"a\", \"b\"},{\"c\", \"d\"}};");
		assertSuccessSerialize("int[] x = new int[]{1,2}; boolean y[] = new boolean[]{true,false}; String[] z[] = new String[][]{{\"a\", \"b\"},{\"c\", \"d\"}};");
		assertSuccessSerialize("int[][] x = {new int[]{1, 2, 3}};");
	}

	@Test
	public void testCast() throws IOException {
		assertSuccessSerialize("int[] x = {}; int[] y = x; assert x == y;");
		assertSuccessSerialize("int[] x = {}; int[] y = (int[])x; assert x == y;");
		assertFailSerialize("byte[] x = {}; int[] y = x;");
		assertFailSerialize("byte[] x = {}; int[] y = (int[])x;");
		assertSuccessSerialize("class A{} class B extends A{} B[] x = {new B()}; A[] y = x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		assertSuccessSerialize("class A{} class B extends A{} A[] x = new B[]{new B()}; B[] y = (B[])x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
	}

	@Test
	public void testExpressions() throws IOException {
		assertSuccessSerialize("int[] x = {0, 1, 2}; assert x.length == 3; assert x[0] == 0; assert x[1] == 1; assert x[2] == 2;");
		assertSuccessSerialize("int[][] x = {{0, 1, 2}, {3, 5, 6}, {7, 8, 9}}; int y = x[0][0] + x[2][2]; assert y == 9;");
		assertSuccessSerialize("int[][] x = {null};");
		assertSuccessSerialize("String[] x = {null}; assert x[0] == null;");
		assertSuccessSerialize("String[][] x = {null}; assert x[0] == null;");
		assertSuccessSerialize("int[][][][][][][][][][] x = {null, {null, {null, {null, {null, {null, {null, {null, {null, { 1 }}}}}}}}}}; assert x[1] != null;");

		assertSuccessSerialize("int[] x = new int[]{0}; x[0]++; assert x[0] == 1;");
		assertSuccessSerialize("int[] x = new int[]{1}; x[0] = 0; assert x[0] == 0;");
		assertSuccessSerialize("String[] x = {null}; x[0] = \"\"; x[0] += 0.000; x[0] += 3L; assert x[0].equals(\"0.03\");");
	}
}