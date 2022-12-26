import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestStatic extends HiTest {
	@Test
	public void test() throws IOException {
		// statement field
		assertFailCompile("final static int x = 0;");

		// class field
		assertSuccessSerialize("class C{static int x;} assert C.x == 0; C.x = 1; assert C.x == 1;");
		assertSuccessSerialize("class C{static int x = 1 + 1;} C.x = 2; C.x--; assert C.x == 1;");
		assertSuccessSerialize("class C1{static int x = 1; class C2{static int x = 2; class C3{static int x = 3;}}} assert C1.C2.C3.x == 3;");

		// class initializer
		assertSuccessSerialize("class C{static int x; static{x = 1;}} assert C.x == 1;");
		assertSuccessSerialize("class C{static int x = 0; {x = 1;}} assert C.x == 0;");
		assertFailCompile("class C{static{x = 1;} static int x = 0;} assert C.x == 0;");

		// method
		assertSuccessSerialize("class C{static int x = 1; static int get(){return x;}}; assert C.get() == 1;");
		assertFailSerialize("class C{int x = 1; int get(){return x;}}; C.get();");
		assertFailSerialize("class C{int x = 1; static int get(){return x;}}; C.get();");
		assertFailSerialize("class C{static int x = 1; int get(){return x;}}; C.get();");
	}
}