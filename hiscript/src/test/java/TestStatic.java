import org.junit.jupiter.api.Test;

public class TestStatic extends HiTest {
	@Test
	public void testClass() {
		assertSuccess("class A{static class B{static class C{}}} A.B.C c = new A.B.C(); assert c instanceof A.B.C;");
		assertSuccess("class A{static class B{} static class C extends B{}} class D extends A.C{} A.C d = new D(); assert d instanceof A.B;");
	}

	@Test
	public void testStatementField() {
		assertSuccess("class A{static class B{static class C{static int x = 1;}}} assert A.B.C.x == 1;");
		assertSuccess("class A{static int a = 1; static class B{static int b = 2; static class C{static int c = 4; static int m(int d) {return a+b+c+d;}}}} assert A.B.C.m(8) == 15;");
		assertSuccess("class A{static int x = 1; static class B{static int x = 2; static class C{static int x = 3; static int m() {return x;}}}} assert A.B.C.m() == 3;");
		assertFailCompile("final static int x = 0;", //
				"modifier 'static' is not allowed");
		assertFailCompile("class A{static void m(static int x){}}", //
				"modifier 'static' is not allowed");
		assertFailCompile("class A{static void m(){static int x = 0;}}", //
				"modifier 'static' is not allowed");
	}

	@Test
	public void testClassField() {
		assertSuccess("class C{static int x;} assert C.x == 0; C.x = 1; assert C.x == 1;");
		assertSuccess("class C{static int x = -1 - 1;} C.x = 2; C.x--; assert C.x == 1;");
		assertSuccess("class C1{static int x = 1; class C2{static int x = 2; class C3{static int x = 3;}}} assert C1.C2.C3.x == 3;");
	}

	@Test
	public void testClassInitializer() {
		// static
		assertSuccess("class A{static{}}");
		assertSuccess("class C{static int x; static{x = 1;}} assert C.x == 1;");
		assertSuccess("class C{static int x = 0; {x = 1;}} assert C.x == 0;"); // static field, non-static initializer
		assertSuccess("class C{static{x = 1;} static int x; static {assert x == 1;}} assert C.x == 1;"); // initializer and field order
		assertSuccess("class C{static{x = 1;} static int x = 0; static {assert x == 0;}} assert C.x == 0;"); // initializer and field order
		assertFailCompile("class C{int x = 1; static{x++;}};", //
				"non-static field 'x' cannot be accessed from static context");
		assertFailCompile("int x = 1; class C{static{x++;}};", //
				"non-static field 'x' cannot be accessed from static context");
		// TODO allow static in inner classes??? assertFailCompile("class A{class B{static{}}}");
		assertSuccess("class C{static int x = 1; {x++;}}; new C(); new C(); assert C.x == 3;");
		assertSuccess("static class A{static{new B(); B.x++;}} static class B{static int x = 0; static{new A(); x++;}} new B(); assert B.x == 2;");

		assertSuccess("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} new B(); assert A.x == 1; assert B.x == 2;");
		assertSuccess("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} new A(); assert A.x == 2; assert B.x == 1;");
		assertSuccess("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} assert A.x == 2; assert B.x == 1;");
		assertSuccess("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} assert B.x == 2; assert A.x == 1;");

		// non static
		assertSuccess("class A{{}}");
		assertSuccess("class C{int x; {x = 1;}} assert new C().x == 1;");
		assertSuccess("class C{{x = 1;} int x; {assert x == 1;}} assert new C().x == 1;"); // initializer and field order
		assertSuccess("class C{{x = 1;} int x = 0; {assert x == 0;}} assert new C().x == 0;"); // initializer and field order
		assertSuccess("int x = 1; class C{{x++;} {assert x == 2;}}; new C(); assert x == 2;");
		assertSuccess("class C{int x = 1; {x++;}}; new C(); new C(); assert new C().x == 2;");
		assertSuccess("class A{{new B().x++;}} class B{int x = 0;} new A(); assert new B().x == 0;");
	}

	@Test
	public void testMethod() {
		assertSuccess("class C{static int x = 1; static int get(){return x;}}; assert C.get() == 1;");
		assertSuccess("class A{static class B{static class C{static int c() {return b()+1;}} static int b() {return a()+1;}} static int a() {return 1;}} assert A.a() == 1; assert A.B.b() == 2; assert A.B.C.c() == 3;");
		assertFailCompile("class C{int x = 1; int get(){return x;}}; C.get();", //
				"non-static method 'get()' cannot be referenced from a static context");
		assertFailCompile("class C{static int x = 1; int get(){return x;}}; C.get();", //
				"non-static method 'get()' cannot be referenced from a static context");
		assertFailCompile("static class C{int x = 1; static int get(){return x;}};", //
				"non-static field 'x' cannot be accessed from static context");
		assertFailCompile("int x = 1; class C{static int get(){return x;}};", //
				"non-static field 'x' cannot be accessed from static context");
	}
}