import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestArrays extends HiTest {
	@Test
	public void testNew() throws IOException {
		assertSuccessSerialize("int[] x; boolean y[]; String[] z[];");
		assertSuccessSerialize("int[] x = {1,2}; boolean y[] = {true,false}; String[] z[] = {{\"a\", \"b\"},{\"c\", \"d\"}};");
		assertSuccessSerialize("int[] x = new int[]{1,2}; boolean y[] = new boolean[]{true,false}; String[] z[] = new String[][]{{\"a\", \"b\"},{\"c\", \"d\"}};");
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
}