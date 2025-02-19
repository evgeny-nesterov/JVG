import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

public class TestClasses extends HiTest {
	@Test
	public void testClasses() {
		assertSuccessSerialize("class C{}");
		assertSuccessSerialize("class C{int c;} assert new C().c == 0;");
		assertSuccessSerialize("class C{int c = 1;} assert new C().c == 1;");
		assertSuccessSerialize("class C{static int c;} assert C.c == 0;");
		assertSuccessSerialize("class C{static int c = 1;} assert C.c == 1;");
		assertFailCompile("class C{class C{}}", //
				"duplicate class: C");
		assertFailCompile("class C{{class C{}}}", //
				"duplicate class: C");

		// abstract
		assertFailCompile("abstract class C{} new C();", //
				"'C' is abstract; cannot be instantiated");
		assertFailCompile("abstract class C{void get();}", //
				"modifier 'abstract' is expected");
		assertSuccessSerialize("abstract class C{} new C(){};");
		assertSuccessSerialize("abstract class C{void get(){}} new C(){void get(){}};");
		assertSuccessSerialize("abstract class C{abstract void get();} new C(){void get(){}};");

		// extends
		assertSuccessSerialize("{class B{B(int x){}} new B(1);} {class B{void get(){}} class C extends B{} new C().get();}");
		assertFailSerialize("class A {A(boolean x){this(x ? 1 : 0);} A(int y){this(y == 1);}} new A(true);");
		assertFailCompile("class A{A(int x){this(x);}} new A(1);", //
				"recursive constructor invocation");
		assertFailCompile("class A extends B{} class B extends A{}", //
				"cyclic inheritance involving B");
		assertFailCompile("class A extends B{} class B extends C{} class C extends A{}", //
				"cyclic inheritance involving C");
		assertFailCompile("final class A{} class B extends A{}", //
				"the type B cannot subclass the final class A");
		assertSuccessSerialize("static class A{static class B{} static class C extends B{}}");
		assertFailCompile("static class A{class B{} static class C extends B{}}", //
				"static class A$C can not extend not static and not top level class");
		assertFailCompile("class A extends B{}", //
				"class 'B' can not be resolved");
		assertFailCompile("class A implements B{}", //
				"class 'B' can not be resolved");

		// initializing order
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert A.a.equals(\"nullBA\"); assert B.b.equals(\"nullB\");");
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert B.b.equals(\"nullAB\"); assert A.a.equals(\"nullA\");");

		// abstract
		assertFailCompile("abstract class A{} new A();", //
				"'A' is abstract; cannot be instantiated");
		assertFailCompile("final abstract class A{}", //
				"abstract class cannot be final");

		// inner classes
		assertSuccessSerialize("class A{class B{}} A.B b = new A().new B();");
		assertSuccessSerialize("class A{class B{class C{}}} A.B.C c = new A().new B().new C();");
		assertFailCompile("class A{class B{}} A.B b = new B();", //
				"class 'B' can not be resolved");
		assertFailCompile("class A{class B{}} A.B b = new A.B();", //
				"'A.B' is not an enclosing class");
		assertFailCompile("class A{class B{}} A.B b = A.new B();", //
				"cannot create");
		assertFailCompile("class A{class B{static class C{}}}", //
				"static declarations in inner classes are not supported");

		// invalid format
		assertFailCompile("class {}", //
				"class name is expected");
		assertFailCompile("class 1A{}", //
				"class name is expected");
		assertFailCompile("class A{", //
				"'}' is expected");
		assertFailCompile("class A extends", //
				"illegal start of type");
		assertFailCompile("class A extends {}", //
				"illegal start of type");
		assertFailCompile("class A{} extends B, ", //
				"unexpected token");
		assertFailCompile("class A implements", //
				"illegal start of type");
		assertFailCompile("class A implements {}", //
				"illegal start of type");
		assertFailCompile("class A implements B, ", //
				"illegal start of type");
		assertFailCompile("class A implements B, {}", //
				"illegal start of type");
		assertFailCompile("class A{A()}", //
				"'{' is expected");

		assertFailCompile("int x = 0; Object o = x.field;", //
				"cannot resolve symbol 'field'");
		assertFailCompile("int x = 0; x.get();", //
				"cannot resolve method 'get' in 'int'");
		assertFailCompile("class I extends int{}", //
				"can not extends primitive class");
		assertFailCompile("class I implements int{}", //
				"interface expected");
		assertFailCompile("class int {}", //
				"class name is expected");
		assertFailCompile("class A{} class B implements A{}", //
				"interface expected");
		assertFailCompile("class A{} class B implements A{}", //
				"interface expected");
		assertFailCompile("interface A{} interface B implements A{}", //
				"interface cannot implements another interfaces");
	}

	@Test
	public void testClassWord() {
		assertSuccessSerialize("interface A{} Class c = A.class; assert c.getName().equals(\"A\");");
		assertSuccessSerialize("interface A{} Class c = A.class; assert c.isInterface() && !c.isArray() && !c.isPrimitive() && !c.isEnum() && !c.isAnnotation() && !c.isAnonymousClass();");
		assertSuccessSerialize("interface A{} assert Class.forName(\"A\") == A.class;");
		assertSuccessSerialize("class A{class B{}} Class c = A.B.class; assert c.getName().equals(\"B\"); assert c.getFullName().equals(\"@root$0A$B\");");
	}

	@Test
	public void testFields() {
		assertSuccessSerialize("class A{} class B extends A{} class C{A a = new B(); A get(){A a = new B(); return a;}} new C().get();");
		assertFailCompile("class A{} class B extends A{} class C{B b = new A();} new C().get();", //
				"incompatible types; found A, required B");
		assertFailCompile("class A{} class B extends A{} class C{B get(){B b = new A(); return b;}} new C().get();", //
				"incompatible types: A cannot be converted to B");
		assertFailCompile("class A{int x; int x;}", //
				"duplicated local variable x");
		assertSuccessSerialize("abstract class A{static int x = 1;} assert A.x == 1;");
		assertSuccessSerialize("abstract class A{static int get(){return 1;}} assert A.get() == 1;");
		assertSuccessSerialize("interface I{static int x = 1;} assert I.x == 1;");
		assertSuccessSerialize("interface I{static int get(){return 1;}} assert I.get() == 1;");
		assertFailCompile("class A{int x = y + 1;}", //
				"cannot resolve symbol 'y'");
		assertFailCompile("int x; class A{x = 1;}", //
				"'}' is expected");
		assertFail("String x = null; int length = x.length();", //
				"null pointer");
	}

	@Test
	public void testInnerClasses() {
		// inner class
		assertSuccessSerialize("class B{int x = 0; {new A();} class A{A(){x++;}} {new A();}} assert new B().x == 2;");
		assertSuccessSerialize("class B{record R(int a);} assert new B().new R(1).a == 1;");
		assertSuccessSerialize("static class B{interface I{int get();}} class A implements B.I{public int get(){return 1;}} assert new A().get() == 1;");
		assertSuccessSerialize("class B{enum E{a,b,c}} E e = B.E.b; assert e == B.E.b;");
		assertSuccessSerialize("class C{} enum E{a} interface I{} record R(int a);");

		// static inner class
		assertSuccessSerialize("class A{static class B{}} A.B b = new A.B(); assert b instanceof A.B;");
		assertSuccessSerialize("static class A{static class B{B(int i){}}} A.B b = new A.B(0); assert b instanceof A.B;");
		assertSuccessSerialize("class A{static class B{static class C{static class D{}}}} A.B.C.D d = new A.B.C.D(); assert d instanceof A.B.C.D;");
		assertFailCompile("class AA{class BB{static class CC{}}}", //
				"static declarations in inner classes are not supported"); // BB is not static
		assertFailCompile("class AA{static class BB{}} BB b;", //
				"class 'BB' can not be resolved");
		assertFailCompile("class A{static class B{}} A.B b = new B();", //
				"class 'B' can not be resolved");
		assertFailCompile("class A{static class B{}} A a = new A(); A.B b = a.new B();", //
				"qualified new of static class");
		assertFailCompile("class A{static class AA{} static class AA{}}", //
				"duplicate nested type AA");
		assertSuccessSerialize("class A {void get(){}} A a = new A(); a.get();");

		// not static inner class
		assertSuccessSerialize("class A{class B{}} A a = new A(); A.B b = a.new B(); new A().new B();");
		assertSuccessSerialize("class A{static class B{class C{C(int i){}}}} A.B b = new A.B(); A.B.C c = b.new C(0); new A.B().new C(0);");
		assertFailCompile("class A{class B{}} A.B b = new A.B();", //
				"'A.B' is not an enclosing class");
		assertSuccessSerialize("class A{class B{class C{int get(){return 123;}} C c = new C();} B b = new B();} A a = new A(); assert a.b != null; assert a.b.c != null; assert a.b.c.get() == 123;");
		assertSuccessSerialize("class A{class B{} B b = new B();} A a = new A(); assert a.b instanceof A.B;");

		// interfaces
		assertSuccessSerialize("interface I{interface I1{}} class X implements I {I1 m() {I1 i = new I1(){}; return i;}} assert new X().m() instanceof I.I1;");

		// super class
		assertSuccessSerialize("class C1 {class C2{}} class X extends C1 {C2 m() {C2 c2 = new C2(); return c2;}} assert new X().m() instanceof C1.C2;");
		assertSuccessSerialize("class C1 {class C2{class C3{}}} class X extends C1 {C1.C2.C3 m() {C1.C2.C3 c3 = new C1().new C2().new C3(); return c3;}} assert new X().m() instanceof C1.C2.C3;");
	}

	@Test
	public void testLocalClasses() {
		assertSuccessSerialize("class A{int x; {class B{B(){x = 2;}} new B();}} assert new A().x == 2;");
		assertSuccessSerialize("class A {class B{A getA(){return A.this;} A.B getB(){return A.B.this;}}} " + //
				"A a = new A(); assert a.new B().getA() == a; A.B b = a.new B(); assert b. getB() == b;");
		assertSuccessSerialize("class A{int x; void m(int x) {class B{B(){A.this.x = x;}} new B();}} A a = new A(); a.m(2); assert a.x == 2;");
		assertSuccessSerialize("class A{int x; A m(int y) {class B{B(){x = y;}} new B(); return this;}} assert new A().m(2).x == 2;");
		assertFailCompile("class A{{static class B{}}}", //
				"modifier 'static' not allowed in local classes");
		assertFailCompile("class A{static {static class B{}}}", //
				"modifier 'static' not allowed in local classes");
		assertFailCompile("static class A{void m(){static class B{}}}", //
				"modifier 'static' not allowed in local classes");
		assertFailCompile("static class A{static void m(){static class B{}}}", //
				"modifier 'static' not allowed in local classes");
	}

	@Test
	public void testInheritance() {
		assertSuccessSerialize("class A{class A1{int get(){return 1;}} int get(){return 2;}} class B extends A{{assert new A1().get() == 4;} class A1{int get(){return 4;}} {assert new A1().get() == 4;} " //
				+ "int get(){assert new A().get() == 2; assert new A1().get() == 4; class A1{int get(){return 5;}} assert new A1().get() == 5; return 6;}}; assert new B().get() == 6;");
		assertSuccessSerialize("class A{int x = 1;} class B extends A{int x = 2; {assert super.x == 1; assert x == 2;} {this.x++; super.x--; assert super.x == 0; assert this.x == 3;}} assert new B().x == 3;");
	}

	@Test
	public void testInterfaces() {
		// extends, implements
		assertSuccessSerialize("interface I{int x1 = 1; final static int x2 = 1; abstract void m1(); void m2(); default void m3(){} static void m4(){}}");
		assertSuccessSerialize("interface I{} class C implements I{} assert new C() instanceof C; assert new C() instanceof I;");
		assertFailCompile("interface I{} new I();", //
				"cannot create object from interface 'I'");
		assertFailCompile("interface I{I{}}", //
				"not a statement");
		assertFailCompile("interface I{int i;}", //
				"variable 'i' might not have been initialized");
		assertFailCompile("interface I{default void m();}", //
				"invalid 'default' modification");
		assertFailCompile("interface I{void m(){}}", //
				"modifier 'private' is expected");
		assertFailCompile("interface I{abstract void m(){}}", //
				"';' is expected");
		assertSuccessSerialize("interface I1{} interface I2 extends I1{} class C implements I2{} I1 c = new C(); assert c instanceof C; assert c instanceof I1; assert c instanceof I2;");
		assertSuccessSerialize("interface I1{} interface I2{} interface I3 extends I1{} interface I12 extends I1, I2{} class C implements I12, I3{} Object c = new C(); assert c instanceof C; assert c instanceof I1; assert c instanceof I2; assert c instanceof I3; assert c instanceof I12;");
		assertSuccessSerialize("interface I{void m();} class C implements I{void m(){}}");
		assertFailCompile("interface I{void m();} class C implements I{}", //
				"abstract method m() is implemented in non-abstract class");
		assertFailCompile("interface I{} class C extends I{}", //
				"cannot extends interface");
		assertFailCompile("interface I{} interface C implements I{}", //
				"interface cannot implements another interfaces");
		assertSuccessSerialize("interface I{void m();} abstract class A implements I{}");
		assertSuccessSerialize("interface I{void m();} abstract class A implements I{} class C extends A implements I{void m(){}} new C(); new A(){void m(){}};");
		assertFailCompile("class C{interface A extends B{} interface B extends A{}}", //
				"cyclic inheritance involving C$A", "cyclic inheritance involving C$B");
		assertFailCompile("interface A extends B{} interface B extends A{}", //
				"class 'B' can not be resolved");
		assertSuccessSerialize("interface A{int x = 1;} class B implements A{} assert new B().x == 1;");
		assertSuccessSerialize("interface A{int x = 1;} class B implements A{} assert B.x == 1;");

		// fields
		assertFailCompile("interface I{int c;}", //
				"variable 'c' might not have been initialized");
		assertSuccessSerialize("interface I{int c = 1, d = 0;} assert I.c == 1;");
		assertFailCompile("interface I{static int c;}", //
				"variable 'c' might not have been initialized");
		assertSuccessSerialize("interface I{static int c = 1;} assert I.c == 1;");
		assertFailCompile("interface I{int c = 0;} I.c = 1;", //
				"cannot assign value to final variable");
		assertFailCompile("interface I{ { System.println(\"\"); } }", //
				"interface cannot have initializers");
		assertFailCompile("interface I{ static { System.println(\"\"); } }", //
				"interface cannot have initializers");

		// default
		assertSuccessSerialize("interface I{default int get(){return 1;}} class C implements I{}; assert new C().get() == 1;");
		assertFailCompile("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{};", //
				"C inherits unrelated defaults for get() from types I1 and I2");
		assertFailCompile("interface I1{int get();} interface I2{int get();} class C implements I1, I2{}; new C().get();", //
				"abstract method get() is implemented in non-abstract class");
		assertSuccessSerialize("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{int get(){return 3;}}; assert new C().get() == 3;");
		assertSuccessSerialize("interface I1{int get();} interface I2{default int get(){return 2;}} class C implements I1, I2{}; assert new C().get() == 2;");

		// initializing order
		assertSuccessSerialize("interface A1{int a1 = 1; default int a(){return 0;} interface A2{int a2 = a1 + 1; default int a(){return 0;} interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 1; assert A1.A2.a2 == 2; assert A1.A2.A3.a3 == 3;");
		assertSuccessSerialize("interface A1{int a1 = A2.A3.a3 + 1; interface A2{int a2 = a1 + 1; interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 3; assert A1.A2.a2 == 1; assert A1.A2.A3.a3 == 2;");
		assertSuccessSerialize("interface A1{int a1 = A2.a2 + 1; interface A2{int a2 = A3.a3 + 1; interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 3; assert A1.A2.a2 == 2; assert A1.A2.A3.a3 == 1;");

		// multiple implementations
		assertFailCompile("interface A {default int get(){return 1;}} interface B {default int get(){return 2;}} class C implements A, B{}", //
				"C inherits unrelated defaults for get() from types A and B");
		assertFailCompile("interface A {default int get(){return 1;}} interface B {default int get(){return 2;}} class C implements A, B{int get(){return B.this.get();}}", //
				"'B' is not an enclosing class");
		assertSuccessSerialize("interface A {default int get(){return 1;}} interface B {default int get(){return 2;}} class C implements A, B{int get(){return B.super.get();}} assert new C().get() == 2;");

		// failures
		assertFailCompile("final interface A{}", //
				"abstract class cannot be final");
		assertFailCompile("class A{void m(){static class B{}}}", //
				"modifier 'static' not allowed in local classes");

		// invalid format
		assertFailCompile("interface {}", //
				"class name is expected");
		assertFailCompile("interface 1A{}", //
				"class name is expected");
		assertFailCompile("interface A extends", //
				"illegal start of type");
		assertFailCompile("interface A extends {}", //
				"illegal start of type");
		assertFailCompile("interface A extends B, {}", //
				"illegal start of type");

		// private methods
		assertSuccessSerialize("interface A{private void m(){}}");
		assertSuccessSerialize("interface A{private int m(){return 1;} default int get(){this.m(); return m();}} assert new A(){}.get() == 1;");
		assertFailCompile("interface A{private void m(){}} new A(){}.m();", //
				"private method 'm()' is not accessible from outside of interface A");
		assertFailCompile("interface A{private void m();}", //
				"modifier 'private' not allowed here");
		assertFailCompile("interface A{protected void m();}", //
				"modifier 'protected' not allowed here");
		assertFailCompile("interface A{protected void m(){}}", //
				"modifier 'private' is expected");
		assertFailCompile("interface A{void m(){}}", //
				"modifier 'private' is expected");
		assertFailCompile("interface A{public void m(){}}", //
				"modifier 'private' is expected");

		// static methods
		assertSuccessSerialize("interface A{static int m(){return 1;}} assert A.m() == 1;");

		// failures
		assertFailCompile("interface A{A(){}}", //
				"interface cannot have constructors");
		assertFailCompile("interface A{void m(){}}", //
				"modifier 'private' is expected");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class C{int get(int... n){return n.length;}} assert new C().get(1, 2, 3) == 3;");
		assertSuccessSerialize("class O{} class O1 extends O{} class C{int get(O... n){return n.length;}} assert new C().get(new O1(), new O()) == 2;");
		assertSuccessSerialize("class O{} class C{int get(Object... n){return n.length;}} assert new C().get(new O(), \"x\", new Object()) == 3;");
		assertSuccessSerialize("class A{int get(){return 1;}} class B extends A{int get(){return super.get() + 1;}} assert new A().get() == 1; assert new B().get() == 2; ");

		assertFailCompile("class A{} class C extends A{void get(){}} A a = new C(); a.get();", //
				"cannot resolve method 'get' in 'A'");
		assertSuccessSerialize("abstract class A{abstract void get();} class C extends A{void get(){}} A a = new C(); a.get();");

		// signature
		assertSuccessSerialize("class A{String m(int x, String arg){return \"\";}} String s = new A().m(0, null);");
		assertFailCompile("class A{int m(int x, String arg){return 1;}} String s = new A().m(0, null);", //
				"incompatible types: int cannot be converted to String");
		assertFailCompile("class A{Object m(int x, String arg){return null;}} class C{} C s = new A().m(0, null);", //
				"incompatible types: Object cannot be converted to C");
		assertFailCompile("class C{void get(String x){}} new C().get(1);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(byte x){}} new C().get((short)1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(byte x){}} new C().get('1');", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(byte x){}} new C().get(1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(byte x){}} new C().get(1l);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(byte x){}} new C().get(1.0f);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(byte x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(short x){}} new C().get('1');", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(short x){}} new C().get(1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(short x){}} new C().get(1l);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(short x){}} new C().get(1.0f);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(short x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(char x){}} new C().get((byte)1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(char x){}} new C().get((short)1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(char x){}} new C().get(1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(char x){}} new C().get(1l);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(char x){}} new C().get(1.0f);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(char x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(int x){}} new C().get(1l);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(int x){}} new C().get(1.0f);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(int x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(long x){}} new C().get(1.0f);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(long x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(float x){}} new C().get(1.0d);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(int x, byte y, char z){}} new C().get(1, 1, 1);", //
				"cannot resolve method 'get' in 'C'");

		assertFailCompile("class C{void get(int x, int y, int z){}} new C().get(1, 1);", //
				"cannot resolve method 'get' in 'C'");
		assertFailCompile("class C{void get(int x, int y, int z){}} new C().get(1, 1, 1, 1);", //
				"cannot resolve method 'get' in 'C'");

		assertSuccessSerialize("class A{void m(int x, int y){}} class B extends A{void m(int x, byte y){}} new B().m(1, 1);");

		assertFailCompile("class A{void get(){}} class B extends A{int get(){}};", //
				"incompatible return type"); // rewrite method with another return type

		assertSuccessSerialize("class C{void m(int... x){assert false;} void m(int x1, int x2, int x3){}} new C().m(1, 2, 3);");
		assertSuccessSerialize("class A{int m(int x){return 1;} int m(int x, int y, int z){return 2;} int m(int x, int... y){return 3;}} assert new A().m(1,2) == 3;");

		// TODO check incompatible throws of rewrite method
		assertSuccessSerialize("class E1 extends Exception{}; class E2 extends Exception{}; class A{void m() throws E1, E2 {throw new E1();}}");
		assertFailCompile("class E1 extends Exception{}; class E2 extends Exception{}; class A{void m() throws E1 {throw new E2();}}", //
				"unreported exception E2: exception must be caught or declared to be thrown");
		assertFailCompile("class E extends Exception{}; class A{void m() throws E {throw new E(); throw new E();}}", //
				"unreachable statement");

		assertFailCompile("class A{void ()}", //
				"'}' is expected", "unexpected token");
		assertFailCompile("class A{void m()}", //
				"'{' is expected", "'}' is expected");
		assertFailCompile("class A{void m(){}", //
				"'}' is expected");
		assertFailCompile("class A{m(){}}", //
				"invalid method declaration; return type is expected");
		assertFailCompile("class A{void m(){return 1;}}", //
				"incompatible types; found int, required void");
		assertFailCompile("class A{int m(){return;}}", //
				"incompatible types; found void, required int");
		assertFailCompile("class A{int m(){return 1;return 1;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(){return;return;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m() throws {}}", //
				"identifier expected");
		assertFailCompile("class A{void m() throws Exception, {}}", //
				"identifier expected");

		// rewrite
		assertSuccessSerialize("class A{Number get(){return 1;}} class B extends A{Integer get(){return 2;}}");
		assertFailCompile("class A{Integer get(){return 1;}} class B extends A{String get(){return null;}}", //
				"incompatible return type");
		assertFailCompile("class A{Integer get(){return 1;}} class B extends A{Number get(){return null;}}", //
				"incompatible return type");

		assertSuccessSerialize("interface A{Number get();} class B implements A{Integer get(){return 2;}}");
		assertFailCompile("interface A{Integer get();} class B implements A{String get(){return null;}}", //
				"incompatible return type");
		assertFailCompile("interface A{Integer get();} class B implements A{Number get(){return null;}}", //
				"incompatible return type");

		// synchronized
		assertSuccessSerialize("class A{synchronized void m(){int x = 0;}} new A().m();");

		// primitives
		assertFailCompile("boolean x = true; x.toString();", //
				"cannot resolve method 'toString' in 'boolean'");
		assertFailCompile("byte x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'byte'");
		assertFailCompile("short x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'short'");
		assertFailCompile("char x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'char'");
		assertFailCompile("int x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'int'");
		assertFailCompile("long x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'long'");
		assertFailCompile("float x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'float'");
		assertFailCompile("double x = 1; x.toString();", //
				"cannot resolve method 'toString' in 'double'");

		// failures
		assertFailCompile("class A{void m(){return; int x = 1;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m1(){} void m2(){return; m1();}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(){}} class B extends A{void m(){return; super.m();}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(int x, String x){}}", //
				"duplicated argument 'x'", //
				"argument with name 'x' already exists");
		assertFailCompile("class A{abstract void m();}", //
				"abstract method in non-abstract class", //
				"abstract method m() is implemented in non-abstract class");
		assertFailCompile("abstract class A{abstract void m(){}}", //
				"';' is expected");
		assertFailCompile("interface A{native void m();}", //
				"interface methods cannot be native", //
				"modifier 'private' is expected", //
				"interface abstract methods cannot have body");
		assertFailCompile("class A{abstract static void m();}", //
				"illegal combination of modifiers: 'static' and 'abstract'", //
				"abstract method in non-abstract class", //
				"static method cannot be abstract");
		assertFailCompile("int x = 1; x.m();", //
				"cannot resolve method 'm' in 'int'");

		assertFailCompile("class A {void m(Float x){}} new A().m(1);", //
				"cannot resolve method 'm' in 'A'");

		// synchronized
		assertSuccessSerialize("class A{synchronized void m(){int x = 0;}} new A().m();");
	}

	@Test
	public void testMethodPriority() {
		// varargs
		assertSuccessSerialize("class A{void m(int x){} void m(int... x){assert false;} void m(Integer x){assert false;}} new A().m(1);");
		assertSuccessSerialize("class A{void m(int x){assert false;} void m(int... x){} void m(Integer x){assert false;}} new A().m(1, 2);");
		assertSuccessSerialize("class A{void m(int x){assert false;} void m(int... x){} void m(Integer x){assert false;}} new A().m(new Integer(1), new Integer(2));");
		assertSuccessSerialize("class A{void m(int x){assert false;} void m(int... x){assert false;} void m(Integer x){}} new A().m(new Integer(1));");
		assertSuccessSerialize("class A{void m(int x, int y){} void m(int... x){assert false;} void m(Integer x, Integer y){assert false;}} new A().m(1, 2);");
		assertFail("Integer i1 = null, i2 = null; class A{void m(int x){} void m(int... x){} void m(Integer x){}} new A().m(i1, i2);", //
				"null pointer");
		assertFail("Boolean i1 = null, i2 = null; class A{void m(boolean x){} void m(boolean... x){} void m(Boolean x){}} new A().m(i1, i2);", //
				"null pointer");

		// primitive -> box+primitive
		assertSuccessSerialize("class A {void m(Float x){assert false;} void m(float x){}} new A().m(1);");
		assertSuccessSerialize("class A {void m(Float x){assert false;} void m(double x){}} new A().m(1);");
		assertSuccessSerialize("class A {void m(Float x){assert false;} void m(byte x){}} new A().m((byte)1);");
		assertSuccessSerialize("class A {void m(Double x){assert false;} void m(double x){}} new A().m(1.1);");

		assertSuccessSerialize("class A{void m(int x1, int x2){} void m(int... x){assert false;} void m(Integer x1, Integer x2){assert false;}} new A().m(1, 2);");
		assertSuccessSerialize("class A{void m(int x1, int x2){assert false;} void m(int... x){assert false;} void m(Integer x1, Integer x2){}} new A().m(new Integer(1), new Integer(2));");
		assertSuccessSerialize("class A{void m(int x1, int x2){assert true;} void m(float x1, float x2){assert false;}} new A().m(1, 2);");
		assertSuccessSerialize("class A{void m(int x1, int x2){assert false;} void m(float x1, float x2){assert true;}} new A().m(1, 2f);");

		// Ambiguous method call
		assertFailCompile("class A{void m(int x, int... y){} void m(int... x){}} new A().m(1, 2);", //
				"Ambiguous method call. Both m(int x, int[] y) in A and m(int[] x) in A match");
		assertFailCompile("class A{void m(int x, int... y){} void m(int... x){}} class B extends A{} new B().m(1, 2);", //
				"Ambiguous method call. Both m(int x, int[] y) in A and m(int[] x) in A match");

		assertSuccessSerialize("class A {void m(int x, int y){assert true;} void m(float x, float y){assert false;}} new A().m(1, 2);"); // priority strict primitives over cast primitives
		assertSuccessSerialize("class A {void m(int x, Integer y){assert true;} void m(Integer x, int y){assert false;}} new A().m(1, new Integer(2));"); // priority strict over autoboxing
		assertSuccessSerialize("class A {void m(String x, Integer y){assert true;} void m(Object x, Object y){assert false;}} new A().m(\"a\", new Integer(1));"); // priority strict over cast Object
		assertSuccessSerialize("class A {void m(Object x, Object y){assert false;} void m(String x, Integer y){assert true;}} new A().m(\"a\", new Integer(1));"); // priority strict over cast Object
		assertSuccessSerialize("class A {void m(Double x, Integer y){assert true;} void m(Number x, Number y){assert false;}} new A().m(1.0, 1);"); // priority strict over cast
		assertSuccessSerialize("class A {void m(Number x, Number y){assert false;} void m(Double x, Integer y){assert true;}} new A().m(1.0, 1);"); // priority strict over cast

		assertFailCompile("class A{} class B extends A{} class C extends B{} class D{void m(A x, B y){} void m(B x, A y){}} new D().m(new C(), new C());",
				"Ambiguous method call. Both m(A x, B y) in D and m(B x, A y) in D match");
		assertFailCompile("class A{} class B extends A{} class C extends B{} class D{void m(B x, A y){} void m(A x, B y){}} new D().m(new C(), new C());",
				"Ambiguous method call. Both m(B x, A y) in D and m(A x, B y) in D match");
		assertFailCompile("class A{} class B extends A{} class C extends B{} class D{void m(A x, B... y){} void m(B x, A... y){}} new D().m(new C(), new C());",
				"Ambiguous method call. Both m(A x, B[] y) in D and m(B x, A[] y) in D match");
		assertFailCompile("class A{} class B extends A{} class C extends B{} class D{void m(A x, B... y){} void m(B x, A... y){}} new D().m(new B(), new B(), new B());",
				"Ambiguous method call. Both m(A x, B[] y) in D and m(B x, A[] y) in D match");
		assertSuccessSerialize("class A{} class B extends A{} class C extends B{} class D{void m(A x, A... y){assert false;} void m(B x, A... y){}} new D().m(new B(), new B(), new B());");
		assertFailCompile("class A{} class B extends A{} class C extends B{} class D{void m(A x, B... y){} void m(B x, A y1, A y2){}} new D().m(new C(), new C(), new C());",
				"Ambiguous method call. Both m(A x, B[] y) in D and m(B x, A y1, A y2) in D match");
		assertSuccessSerialize("class A{} class B extends A{} class C extends B{} class D{void m(A x, A... y){assert false;} void m(B x, A y1, A y2){}} new D().m(new B(), new B(), new B());");
		assertSuccessSerialize("class A{} class B extends A{} class C extends B{} class D{void m(B x, A y1, A y2){} void m(A x, A... y){assert false;}} new D().m(new B(), new B(), new B());");

		assertSuccessSerialize("class A {void m(int x, int y){assert true;} void m(float x, float y){assert false;}} new A().m(new Integer(1), 2);"); // priority autoboxing (box->primitive) over primitive cast
		assertSuccessSerialize("class A {void m(float x, float y){assert false;} void m(int x, int y){assert true;}} new A().m(new Integer(1), 2);"); // priority autoboxing (box->primitive) over primitive cast
		assertSuccessSerialize("class A {void m(Integer x, int y){assert true;} void m(float x, float y){assert false;}} new A().m(1, 2);"); // priority autoboxing(primitive->box) over primitive cast
		assertSuccessSerialize("class A {void m(float x, float y){assert false;} void m(Integer x, int y){assert true;}} new A().m(1, 2);"); // priority autoboxing(primitive->box) over primitive cast
		assertSuccessSerialize("class A {void m(long x, long y){assert true;} void m(float x, float y){assert false;}} new A().m(1, 2);"); // priority primitive cast over primitive cast
		assertSuccessSerialize("class A {void m(float x, float y){assert false;} void m(long x, long y){assert true;}} new A().m(1, 2);"); // priority primitive cast over primitive cast
		assertSuccessSerialize("class A {void m(long x, long y){assert true;} void m(char x, char y){assert false;}} new A().m(1, 2);"); // priority primitive cast over primitive cast
		assertSuccessSerialize("class A {void m(char x, char y){assert false;} void m(long x, long y){assert true;}} new A().m(1, 2);"); // priority primitive cast over primitive cast
		assertFailCompile("class A {void m(Integer x, int y){} void m(int x, int y){}} new A().m(1, new Integer(2));", //
				"Ambiguous method call. Both m(Integer x, int y) in A and m(int x, int y) in A match.");
		assertFailCompile("class A {void m(Integer x, Integer y){} void m(Integer x,  int y){}} new A().m(1, new Integer(2));", //
				"Ambiguous method call. Both m(Integer x, Integer y) in A and m(Integer x, int y) in A match");

		assertSuccessSerialize("class A {void m(int x, int y){assert true;} void m(Integer x, Integer y){assert false;}} new A().m(1, 2);");
		assertSuccessSerialize("class A {void m(Integer x, Integer y){assert true;} void m(int x, int y){assert false;}} new A().m(new Integer(1), new Integer(2));");
		assertSuccessSerialize("class A {void m(Integer x, int y){assert true;} void m(int x, int y){assert false;}} new A().m(new Integer(1), 2);");
		assertFailCompile("class A {void m(int x, int y){} void m(Integer x, Integer y){}} new A().m(new Integer(1), 2);", //
				"Ambiguous method call. Both m(int x, int y) in A and m(Integer x, Integer y) in A match.");

		// priority primitive cast over primitive cast
		String[] types = {"byte", "short", "char", "int", "long", "float", "double"};
		for (int i1 = 0; i1 < types.length; i1++) {
			String a1 = types[i1];
			for (int i2 = 0; i2 < types.length; i2++) {
				if (i1 != i2) {
					String a2 = types[i2];
					for (int i = 0; i < types.length; i++) {
						String a = types[i];
						boolean cm1 = i == i1 || !a1.equals("char");
						boolean cm2 = i == i2 || !a2.equals("char");
						boolean m1 = i <= i1 && (i1 < i2 || i2 < i || !cm2) && (cm1 || !cm2);
						boolean m2 = i <= i2 && (i2 < i1 || i1 < i || !cm1) && (cm2 || !cm1);
						if (m1 || m2) {
							String ms1 = m1 ? "true" : "false";
							String ms2 = m2 ? "true" : "false";
							assertSuccessSerialize("class A {void m(" + a1 + " x){assert " + ms1 + ";} void m(" + a2 + " x){assert " + ms2 + ";}} new A().m((" + a + ")1);");
						} else {
							assertFailCompile("class A {void m(" + a1 + " x){} void m(" + a2 + " x){}} new A().m((" + a + ")1);", //
									"cannot resolve method 'm' in 'A'");
						}
					}
				}
			}
		}
	}

	@Test
	public void testNativeMethod() {
		//		assertSuccessSerialize("class A{native void m();}");
		//		assertFail("class A{native void m();} new A().m();", //
		//				"native method 'root$0A_void_m' not found");

		class A {
			public void root$0A_int_m_int(RuntimeContext ctx, int value) {
				ctx.setValue(value + 1);
			}

			public void root$0A_void_m(RuntimeContext ctx) {
				ctx.throwRuntimeException("test error");
			}

			public void root$0A_void_error(RuntimeContext ctx) {
				throw new RuntimeException("error");
			}
		}
		nativeObject(new A());
		assertSuccessSerialize("class A{native int m(int value);} assert new A().m(1) == 2;");
		//		assertSuccessSerialize("class A{native void m();} try{new A().m();} catch(Exception e){assert e.getMessage().equals(\"test error\");}");
		//		assertFail("class A{native void error();} new A().error();", //
		//				"error");
	}

	@Test
	public void testConstructors() {
		// signature
		assertSuccessSerialize("class C{C(){}} new C();");
		assertSuccessSerialize("class A{A(int x, String arg){}} new A(0, \"a\"); new A(0, null);");
		assertSuccessSerialize("class C{C(int x, int y){assert false;} C(int x){}} new C(1);");

		assertFailCompile("class C{C(byte x){}} new C((short)1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte x){}} new C('1');", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte x){}} new C(1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte x){}} new C(1l);", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte x){}} new C(1.0f);", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(short x){}} new C('1');", //
				"constructor not found: C");
		assertFailCompile("class C{C(short x){}} new C(1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(short x){}} new C(1l);", //
				"constructor not found: C");
		assertFailCompile("class C{C(short x){}} new C(1.0f);", //
				"constructor not found: C");
		assertFailCompile("class C{C(short x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(char x){}} new C((byte)1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(char x){}} new C((short)1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(char x){}} new C(1);", //
				"constructor not found: C");
		assertFailCompile("class C{C(char x){}} new C(1l);", //
				"constructor not found: C");
		assertFailCompile("class C{C(char x){}} new C(1.0f);", //
				"constructor not found: C");
		assertFailCompile("class C{C(char x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(int x){}} new C(1l);", //
				"constructor not found: C");
		assertFailCompile("class C{C(int x){}} new C(1.0f);", //
				"constructor not found: C");
		assertFailCompile("class C{C(int x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(long x){}} new C(1.0f);", //
				"constructor not found: C");
		assertFailCompile("class C{C(long x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(float x){}} new C(1.0d);", //
				"constructor not found: C");

		assertFailCompile("class C{C(int x, byte y, char z){}} new C(1, 1, 1);", //
				"constructor not found: C");

		// varargs
		assertSuccessSerialize("class C{C(int length, int... n){assert n.length == length;}} new C(0); new C(3, 1, 2, 3);");
		assertSuccessSerialize("class O{} class O1 extends O{} class C{C(O... n){assert n.length == 2;}} new C(new O1(), new O());");
		assertSuccessSerialize("class O{} class C{Object[] o; C(Object... o){this.o = o;}} assert new C().o.length == 0; assert new C(new O(), \"x\", new Object()).o.length == 3;;");
		assertFailCompile("class C{C(String... s){}} new C(1, 2, 3);", //
				"constructor not found: C");
		assertFailCompile("class C{C(int... x, String s){}} new C(1, 2, 3, \"x\");", //
				"constructor not found: C");
		assertFailCompile("class C{C(int... x, int... y){}} new C(/*x=*/1, 2, /*y=*/3);", //
				"constructor not found: C");
		assertFailCompile("class C{C(byte... x){}} new C(1, 2, 3);", //
				"constructor not found: C");
		assertSuccessSerialize("class C{C(int... x){assert false;} C(int x1, int x2, int x3){}} new C(1, 2, 3);");

		// this
		assertSuccessSerialize("class A{A(int x){assert x == 1;} A(int x, int y){this(x); assert y == 2;} } new A(1, 2);");
		assertSuccessSerialize("class A{A(int x){assert x == 1;} A(int x, int y){this(x); assert y == 2;} } new A(1, 2);");
		assertFailCompile("class A{A(){this();}} new A();", //
				"recursive constructor invocation");
		assertFail("class A{A(int x){this(true);} A(boolean x){this(1);}} new A(1);", //
				"recursive constructor invocation"); // recursive constructor invocation
		assertFailCompile("class A{A(){this(1);} A(byte x){}} new A();", //
				"constructor not found: A");

		// super
		assertSuccessSerialize("class A{A(){super();}} new A();");
		assertSuccessSerialize("class A{A(int x){this.x = x;} int x;} class B extends A{B(int y){super(y);}} A a = new B(1); assert a.x == 1;");
		assertSuccessSerialize("class A{A(int x, int y){}} class B extends A{B(int x, byte y){super(x, y);}} new B(1, (byte)1);");

		// failures
		assertFailCompile("class A{A(){return; super();}}", //
				"';' is expected");
		assertFailCompile("class A{A(){super(1);}}", //
				"constructor not found: Object");
		assertFailCompile("class A{A(){this(1);}}", //
				"constructor not found: A");
		assertFailCompile("class A{A(int x, String x){}}", //
				"duplicated argument 'x'");
		assertFailCompile("class A{A(int x){this(x);}}", //
				"recursive constructor invocation");
		assertFail("class C{C(int a){}} Integer x = null; new C(x);", //
				"null pointer");
		assertFail("class C{C(int... a){}} Integer x = null; new C(1, 2, 3, x);", //
				"null pointer");
	}

	@Test
	public void testEnums() {
		assertSuccessSerialize("enum E{e1, e2, e3}; E e = E.e1; e = E.e2; if (e == E.e2) e = E.e3; assert e == E.e3; assert E.e3.ordinal() == 2;");
		assertSuccessSerialize("enum E1{e} enum E2{e;} assert E1.e != E2.e; assert E1.e.name().equals(E2.e.name()); assert !E1.e.equals(E2.e);");
		assertSuccessSerialize("enum E{e1(\"e1\", 0), e2(\"e2\", 1); String name; int number; E(String name, int number){this.name = name; this.number = number;} String getName(){return name;} int getNumber(){return number;}} E e = E.e2; assert e.getName().equals(\"e2\"); assert e.getNumber() == 1;");
		assertSuccessSerialize("enum E{e} assert E.e instanceof Enum; assert E.e instanceof E;");
		assertSuccessSerialize("enum E{} E e = null;");
		assertSuccessSerialize("enum E{A, a}");
		assertSuccessSerialize("enum E{e1, e2(1); int x; E(){} E(int x){this.x = x;}} assert E.e1.x == 0; assert E.e2.x == 1;");
		assertFail("enum E{e1; E(){throw new RuntimeException(\"error\");}} E e = E.e1;", //
				"error");
		assertFail("enum E{e1; {throw new RuntimeException(\"error\");}} E e = E.e1;", //
				"error");

		// failures
		assertFailCompile("enum {}", //
				"enum name is expected");
		assertFailCompile("enum E{", //
				"'}' is expected");
		assertFailCompile("enum E{1}", //
				"'}' is expected");
		assertFailCompile("enum E{e(}", //
				"')' is expected");
		assertFailCompile("enum E{e(1)}", //
				"invalid constructor arguments");
		assertFailCompile("enum E{e(1,) E(int x, int y){}}", //
				"expression expected");
		assertFailCompile("enum E{e(1,); E(int x, int y){}}", //
				"expression expected");
		assertFailCompile("enum E{e(1,true) E(int x, int y){}}", //
				"expected ',', '(' or ';'");
		assertFailCompile("enum E{e(); E(int a){}}", //
				"invalid constructor arguments");
		assertFailCompile("enum E{e(true); E(int a){}}", //
				"invalid constructor arguments");
		assertFailCompile("enum E1{e1} enum E2 extends E1{e2}", //
				"'{' is expected");
		assertFailCompile("enum E1{e1} new E1();", //
				"enum types cannot be instantiated");
		assertFailCompile("enum E1{e1} E1 e = new Object();", //
				"incompatible types: Object cannot be converted to E1");
		assertFailCompile("enum E1{A, b, C, A, d, C, e, f, g}", //
				"variable 'A' is already defined in the scope");
	}

	@Test
	public void testRecords() {
		assertSuccessSerialize("record Rec(int a); Rec r = new Rec(1);");
		assertSuccessSerialize("record Rec(int abc); Rec r = new Rec(1);");
		assertSuccessSerialize("class A{}; record Rec(A a, A b); new Rec(new A(), new A());");
		assertSuccessSerialize("record Rec(int a, String field); Rec rec = new Rec(1, \"abc\"); assert rec.getA() == 1; assert rec.getField().equals(\"abc\");");
		assertSuccessSerialize("record Rec(int a, int b){Rec(int a){this.a = a; b = 2;}}; Rec rec1 = new Rec(1, 2); Rec rec2 = new Rec(1); assert rec2.getA() == 1; assert rec2.getB() == 2;");
		assertSuccessSerialize("record Rec(int a, int b){int c; Rec(int c){a = 1; b = 2; this.c = c;} int getC(){return c;}}; Rec rec = new Rec(3); assert rec.getA() == 1; assert rec.getB() == 2; assert rec.getC() == 3;");

		// equals
		assertSuccessSerialize("record Rec(int a, String b); assert new Rec(1, \"a\").equals(new Rec(1, \"a\"));");
		assertFailSerialize("record Rec(int a, String b); assert new Rec(1, \"a\").equals(new Rec(2, \"a\"));");
		assertSuccessSerialize("record Rec(int a, String b); assert new Rec(1, \"abc\").equals(new Rec(1, \"abc\"));");
		assertSuccessSerialize("class O{} O o = new O(); record Rec(O o); assert new Rec(o).equals(new Rec(o));");
		assertFailSerialize("class O{} record Rec(O o); assert new Rec(new O()).equals(new Rec(new O()));");
		assertSuccessSerialize("class O{int a; O(int a){this.a = a;} public boolean equals(Object o){return a == ((O)o).a;}} record Rec(O o); assert new Rec(new O(1)).equals(new Rec(new O(1)));");

		// hashcode
		assertSuccessSerialize("class O{} record Rec(O o, int x, String a); new Rec(new O(), 1, \"x\").hashCode();");

		// instanceof
		assertSuccessSerialize("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(int a, String b, _) rec){assert a == 1; a = 2; assert b.equals(\"abc\"); assert rec.getA() == 2; assert rec.getB().equals(\"abc\");}");
		assertSuccessSerialize("record Rec(int a, Object b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(byte a, String b, _) rec){assert a == 1; a = 2; assert b.equals(\"abc\"); assert rec.getA() == 2; assert rec.getB().equals(\"abc\");}");
		assertSuccessSerialize("record Rec(int a){int b; Rec(int b){a = 1; this.b = b;} int getB(){return b;}}; Rec r = new Rec(3); if(r instanceof Rec(int b) rec) {} else {assert false;}}");
		assertFailCompile("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(byte a) rec);", //
				"record constructor not found: Rec(byte)");
		assertFailCompile("record Rec(byte a); Object r = new Rec(1); if(r instanceof Rec(int a) rec);", //
				"record constructor not found: Rec(int)");
		assertFailCompile("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(String b, String a, String _) rec);", //
				"record constructor not found: Rec(String, String, String)");
		assertFailCompile("record Rec(int a); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(int a, int b) rec);", //
				"record constructor not found: Rec(int, int)");
		assertFailCompile("class Rec{Rec(int a){}} Object r = new Rec(a); if(r instanceof Rec(int a) rec);", //
				"cannot resolve symbol 'a'");

		// switch-case
		assertSuccessSerialize("record Rec(int a); Object o = new Rec(1); switch(o){case \"o\": assert false; case Rec(int a) r: assert a == 1; a = 2; assert r.getA() == 2; break;} assert ((Rec)o).getA() == 2;");
		assertSuccessSerialize("record Rec(int param); Object o = new Rec(1); switch(o){case \"o\": assert false; case Rec(int param) r when param == 1: assert param == 1; param = 2; assert r.getParam() == 2; break;} assert ((Rec)o).getParam() == 2;");
		// duplicated local variable a
		assertFailCompile("record Rec(int a); Object o = new Rec(1); boolean a = true; switch(o){case \"o\": assert false; break; case Rec(int a) r when a == 1: assert a == 1; a = 2; assert r.getA() == 2; break;} assert ((Rec)o).getA() == 2;", //
				"duplicated local variable a");
		assertFailCompile("record R1(int x); record R2(int x) extends R1;", //
				"'{' is expected");
		assertSuccessSerialize("record R(int x); int y = switch(new R(2)){case R(int x) when x == 11 -> x; case R(int x) when x > 1 -> 22;}; assert y == 22;");

		// rewrite get/set method
		assertFail("record R(boolean x){boolean getX(){throw new RuntimeException(\"exception in record rewritten method\");}} R r = new R(true); boolean x = r.getX();", //
				"exception in record rewritten method");
		assertFail("record R(boolean x){void setX(boolean x){throw new RuntimeException(\"exception in record rewritten method\");}} R r = new R(true);", //
				"exception in record rewritten method");

		// failures
		assertFailCompile("record;", //
				"record name is expected");
		assertFailCompile("record Rec;", //
				"'(' is expected");
		assertFailCompile("record Rec(int a, long a);", //
				"duplicated argument 'a'");
		assertFailCompile("record (int a);", //
				"record name is expected");
		assertFailCompile("record Rec();", //
				"record argument expected");
		assertFailCompile("record Rec(){int a;};", //
				"record argument expected");
	}

	@Test
	public void testStackOverflow() {
		assertFail("class A {\n\tvoid m1(int i) {\n\t\tm2(i+1);\n\t}\n\n\tvoid m2(int i) {\n\t\tm1(i+1);\n\t}\n}\nnew A().m1(1);", //
				StackOverflowError.class);
	}

	@Test
	public void testVarargs() {
		// methods
		assertFailCompile("class A{void m(int... x){}} new A().m(\"\");", //
				"cannot resolve method 'm' in 'A'");
		assertFailCompile("class A{void m(int... x){}} new A().m(1, 2, 3, true);", //
				"cannot resolve method 'm' in 'A'");
		assertFailCompile("class A{void m(int... x){}} new A().m(new double[]{1});", //
				"cannot resolve method 'm' in 'A'");
		assertFailCompile("class A{void m(int... x){}} new A().m(new double[]{});", //
				"cannot resolve method 'm' in 'A'");

		// constructors
		assertFailCompile("class C{C(int.. x){}}", //
				"unexpected token");
	}

	@Test
	public void testModifiers() {
		// fields
		assertFailCompile("default int x;", //
				"modifier 'default' is not allowed");
		assertFailCompile("native int x;", //
				"modifier 'native' is not allowed");
		assertFailCompile("abstract int x;", //
				"modifier 'abstract' is not allowed");
		assertFailCompile("synchronized int x;", //
				"'(' is expected");
		assertFailCompile("interface I{protected int x = 0;};", //
				"modifier 'protected' not allowed here");

		// classes
		assertFailCompile("private class A{};", //
				"modifier 'private' not allowed here");
		assertSuccessSerialize("class A{private class B{}};");
		assertFailCompile("protected class A{};", //
				"modifier 'protected' not allowed here");
		assertFailCompile("class A{protected class B{}};", //
				"modifier 'protected' not allowed here");
		assertFailCompile("final abstract class A{};", //
				"abstract class cannot be final");

		// interfaces
		assertFailCompile("final interface A{};", //
				"abstract class cannot be final");

		// constructors
		assertFailCompile("class A{default A(){}};", //
				"modifier 'default' is not allowed");
		assertFailCompile("class A{native A(){};}", //
				"modifier 'native' is not allowed");
		assertFailCompile("class A{abstract A(){};}", //
				"modifier 'abstract' is not allowed");
		assertFailCompile("class A{static A(){};}", //
				"modifier 'static' is not allowed");
		assertFailCompile("class A{synchronized A(){};}", //
				"modifier 'synchronized' is not allowed");

		// methods
		assertFailCompile("class A{default void m(){};", //
				"invalid 'default' modification");
		assertFailCompile("class A{native void m(){};", //
				"';' is expected");
		assertFailCompile("class A{abstract void m(){};", //
				"';' is expected", //
				"abstract method in non-abstract class", //
				"abstract method m() is implemented in non-abstract class");
		assertFailCompile("class A{abstract void m(int x, int... y){};", //
				"';' is expected", //
				"abstract method in non-abstract class", //
				"abstract method m(int, int...) is implemented in non-abstract class");
		assertFailCompile("abstract class A{final abstract void m();}", //
				"illegal combination of modifiers: 'abstract' and 'final'");
		assertFailCompile("abstract class A{abstract static void m();}", //
				"illegal combination of modifiers: 'static' and 'abstract'");
		assertFailCompile("interface I{protected void m();};", //
				"modifier 'protected' not allowed here");
		assertFailCompile("interface I{protected static void m();};", //
				"modifier 'protected' not allowed here");

		// method arguments (only final)
		assertFailCompile("class A{void m(public int x){}};", //
				"modifier 'public' is not allowed");
		assertFailCompile("class A{void m(protected int x){}};", //
				"modifier 'protected' is not allowed");
		assertFailCompile("class A{void m(private int x){}};", //
				"modifier 'private' is not allowed");
		assertFailCompile("class A{void m(default int x){}};", //
				"modifier 'default' is not allowed");
		assertFailCompile("class A{void m(native int x){}};", //
				"modifier 'native' is not allowed");
		assertFailCompile("class A{void m(abstract int x){}};", //
				"modifier 'abstract' is not allowed");
		assertFailCompile("class A{void m(static int x){}};", //
				"modifier 'static' is not allowed");
		assertFailCompile("class A{void m(synchronized int x){}};", //
				"')' is expected");
	}

	@Test
	public void testAccess() {
		// fields access
		assertSuccessSerialize("class A{public int x = 1;} class B extends A{int y = x;} assert new B().y == 1;");
		assertSuccessSerialize("class A{public int x = 1;} A a = new A(); class B{int y = a.x;} assert new B().y == 1;");

		assertSuccessSerialize("class A{protected int x = 1;} class B extends A{int y = x;} assert new B().y == 1;");

		assertFailCompile("class A{private int x = 1;} class B extends A{int y = x;}", //
				"'x' has private access in 'A'");
		assertSuccessSerialize("class A{private int x = 1;} class B extends A{int y = super.x;} assert new B().y == 1;");
		assertFailCompile("class A{private int x = 1;} class B extends A{int y = this.x;}", //
				"'x' has private access in 'A'");

		// class access
		assertFailCompile("class A{private class B{}} A a = new A(); A.B b = a.new B();", //
				"class 'A$B' has private access");
	}
}