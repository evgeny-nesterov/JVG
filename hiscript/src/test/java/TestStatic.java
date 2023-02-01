import org.junit.jupiter.api.Test;

public class TestStatic extends HiTest {
	@Test
	public void testClass() {
		assertSuccessSerialize("class A{static class B{static class C{}}} A.B.C c = new A.B.C(); assert c instanceof A.B.C;");
		assertSuccessSerialize("class A{static class B{} static class C extends B{}} class D extends A.C{} A.C d = new D(); assert d instanceof A.B;");
	}

	@Test
	public void testStatementField() {
		assertSuccessSerialize("class A{static class B{static class C{static int x = 1;}}} assert A.B.C.x == 1;");
		assertSuccessSerialize("class A{static int a = 1; static class B{static int b = 2; static class C{static int c = 4; static int m(int d) {return a+b+c+d;}}}} assert A.B.C.m(8) == 15;");
		assertSuccessSerialize("class A{static int x = 1; static class B{static int x = 2; static class C{static int x = 3; static int m() {return x;}}}} assert A.B.C.m() == 3;");
		assertFailCompile("final static int x = 0;");
		assertFailCompile("class A{static void m(static int x){}}");
		assertFailCompile("class A{static void m(){static int x = 0;}}");
	}

	@Test
	public void testClassField() {
		assertSuccessSerialize("class C{static int x;} assert C.x == 0; C.x = 1; assert C.x == 1;");
		assertSuccessSerialize("class C{static int x = -1 - 1;} C.x = 2; C.x--; assert C.x == 1;");
		assertSuccessSerialize("class C1{static int x = 1; class C2{static int x = 2; class C3{static int x = 3;}}} assert C1.C2.C3.x == 3;");
	}

	@Test
	public void testClassInitializer() {
		assertSuccessSerialize("class A{static{}}");
		assertSuccessSerialize("class C{static int x; static{x = 1;}} assert C.x == 1;");
		assertSuccessSerialize("class C{static int x = 0; {x = 1;}} assert C.x == 0;"); // static field, non-static initializer
		assertFailCompile("class C{static{x = 1;} static int x = 0;}"); // initializer and field order
		assertFailCompile("class C{int x = 1; static{x++;}};"); // non-static field, static initializer
		assertFailCompile("int x = 1; class C{static{x++;}};");
		// TODO allow static in inner classes??? assertFailCompile("class A{class B{static{}}}");
		assertSuccessSerialize("class C{static int x = 1; {x++;}}; new C(); new C(); assert C.x == 3;");
		assertSuccessSerialize("static class A{static{new B(); B.x++;}} static class B{static int x = 0; static{new A(); x++;}} new B(); assert B.x == 2;");

		assertSuccessSerialize("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} new B(); assert A.x == 1; assert B.x == 2;");
		assertSuccessSerialize("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} new A(); assert A.x == 2; assert B.x == 1;");
		assertSuccessSerialize("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} assert A.x == 2; assert B.x == 1;");
		assertSuccessSerialize("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} assert B.x == 2; assert A.x == 1;");
	}

	@Test
	public void testMethod() {
		assertSuccessSerialize("class C{static int x = 1; static int get(){return x;}}; assert C.get() == 1;");
		assertSuccessSerialize("class A{static class B{static class C{static int c() {return b()+1;}} static int b() {return a()+1;}} static int a() {return 1;}} assert A.a() == 1; assert A.B.b() == 2; assert A.B.C.c() == 3;");
		assertFailCompile("class C{int x = 1; int get(){return x;}}; C.get();");
		assertFailCompile("class C{static int x = 1; int get(){return x;}}; C.get();");
		assertFailCompile("static class C{int x = 1; static int get(){return x;}};");
		assertFailCompile("int x = 1; class C{static int get(){return x;}};");
	}
}