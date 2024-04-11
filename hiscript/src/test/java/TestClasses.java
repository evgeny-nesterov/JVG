import org.junit.jupiter.api.Test;

public class TestClasses extends HiTest {
	@Test
	public void testClasses() {
		assertSuccessSerialize("class C{}");
		assertSuccessSerialize("class C{int c;} assert new C().c == 0;");
		assertSuccessSerialize("class C{int c = 1;} assert new C().c == 1;");
		assertSuccessSerialize("class C{static int c;} assert C.c == 0;");
		assertSuccessSerialize("class C{static int c = 1;} assert C.c == 1;");
		assertFailCompile("class C{class C{}}"); // duplicate
		assertFailCompile("class C{{class C{}}}"); // duplicate

		// abstract
		assertFailCompile("abstract class C{} new C();");
		assertFailCompile("abstract class C{void get();}");
		assertSuccessSerialize("abstract class C{} new C(){};");
		assertSuccessSerialize("abstract class C{void get(){}} new C(){void get(){}};");
		assertSuccessSerialize("abstract class C{abstract void get();} new C(){void get(){}};");

		// extends
		assertSuccessSerialize("{class B{B(int x){}} new B(1);} {class B{void get(){}} class C extends B{} new C().get();}");
		assertFailSerialize("class A {A(boolean x){this(x ? 1 : 0);} A(int y){this(y == 1);}} new A(true);");
		assertFailCompile("class A{A(int x){this(x);}} new A(1);");
		assertFailCompile("class A extends B{} class B extends A{}");
		assertFailCompile("class A extends B{} class B extends C{} class C extends A{}");
		assertFailCompile("final class A{} class B extends A{}");
		assertSuccessSerialize("static class A{static class B{} static class C extends B{}}");
		assertFailCompile("static class A{class B{} static class C extends B{}}");
		assertFailCompile("class A extends B{}");
		assertFailCompile("class A implements B{}");

		// initializing order
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert A.a.equals(\"nullBA\"); assert B.b.equals(\"nullB\");");
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert B.b.equals(\"nullAB\"); assert A.a.equals(\"nullA\");");

		// abstract
		assertFailCompile("abstract class A{} new A();");
		assertFailCompile("final abstract class A{}");

		// invalid format
		assertFailCompile("class {}");
		assertFailCompile("class 1A{}");
		assertFailCompile("class A{");
		assertFailCompile("class A extends");
		assertFailCompile("class A extends {}");
		assertFailCompile("class A{} extends B, ");
		assertFailCompile("class A implements");
		assertFailCompile("class A implements {}");
		assertFailCompile("class A implements B, ");
		assertFailCompile("class A implements B, {}");
		assertFailCompile("class A{A()}");
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
		assertFailCompile("class A{} class B extends A{} class C{B b = new A();} new C().get();");
		assertFailCompile("class A{} class B extends A{} class C{B get(){B b = new A(); return b;}} new C().get();");
		assertFailCompile("class A{int x; int x;}");
		assertSuccessSerialize("abstract class A{static int x = 1;} assert A.x == 1;");
		assertSuccessSerialize("abstract class A{static int get(){return 1;}} assert A.get() == 1;");
		assertSuccessSerialize("interface I{static int x = 1;} assert I.x == 1;");
		assertSuccessSerialize("interface I{static int get(){return 1;}} assert I.get() == 1;");
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
		assertFailCompile("class AA{class BB{static class CC{}}}"); // BB is not static
		assertFailCompile("class AA{static class BB{}} BB b;");
		assertFailCompile("class A{static class B{}} A.B b = new B();");
		assertFailCompile("class A{static class B{}} A a = new A(); A.B b = a.new B();");
		assertFailCompile("class A{static class AA{} static class AA{}}");
		assertSuccessSerialize("class A {void get(){}} A a = new A(); a.get();");

		// not static inner class
		assertSuccessSerialize("class A{class B{}} A a = new A(); A.B b = a.new B(); new A().new B();");
		assertSuccessSerialize("class A{static class B{class C{C(int i){}}}} A.B b = new A.B(); A.B.C c = b.new C(0); new A.B().new C(0);");
		assertFailCompile("class A{class B{}} A.B b = new A.B();");
		assertSuccessSerialize("class A{class B{class C{int get(){return 123;}} C c = new C();} B b = new B();} A a = new A(); assert a.b != null; assert a.b.c != null; assert a.b.c.get() == 123;");
		assertSuccessSerialize("class A{class B{} B b = new B();} A a = new A(); assert a.b instanceof A.B;");

		// interfaces
		assertSuccessSerialize("interface I{interface I1{}} class X implements I {I1 m() {I1 i = new I1(){}; return i;}} assert new X().m() instanceof I.I1;");
	}

	@Test
	public void testLocalClasses() {
		assertSuccessSerialize("class A{int x; {class B{B(){x = 2;}} new B();}} assert new A().x == 2;");
		assertSuccessSerialize("class A {class B{A getA(){return A.this;} A.B getB(){return A.B.this;}}} " + //
				"A a = new A(); assert a.new B().getA() == a; A.B b = a.new B(); assert b. getB() == b;");
		assertSuccessSerialize("class A{int x; void m(int x) {class B{B(){A.this.x = x;}} new B();}} A a = new A(); a.m(2); assert a.x == 2;");
		assertSuccessSerialize("class A{int x; A m(int y) {class B{B(){x = y;}} new B(); return this;}} assert new A().m(2).x == 2;");
		assertFailCompile("class A{{static class B{}}}");
		assertFailCompile("class A{static {static class B{}}}");
		assertFailCompile("static class A{void m(){static class B{}}}");
		assertFailCompile("static class A{static void m(){static class B{}}}");
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
		assertFailCompile("interface I{} new I();");
		assertFailCompile("interface I{I{}}");
		assertFailCompile("interface I{int i;}");
		assertFailCompile("interface I{default void m();}");
		assertFailCompile("interface I{void m(){}}");
		assertFailCompile("interface I{abstract void m(){}}");
		assertSuccessSerialize("interface I1{} interface I2 extends I1{} class C implements I2{} I1 c = new C(); assert c instanceof C; assert c instanceof I1; assert c instanceof I2;");
		assertSuccessSerialize("interface I1{} interface I2{} interface I3 extends I1{} interface I12 extends I1, I2{} class C implements I12, I3{} Object c = new C(); assert c instanceof C; assert c instanceof I1; assert c instanceof I2; assert c instanceof I3; assert c instanceof I12;");
		assertSuccessSerialize("interface I{void m();} class C implements I{void m(){}}");
		assertFailCompile("interface I{void m();} class C implements I{}");
		assertFailCompile("interface I{} class C extends I{}");
		assertFailCompile("interface I{} interface C implements I{}");
		assertSuccessSerialize("interface I{void m();} abstract class A implements I{}");
		assertSuccessSerialize("interface I{void m();} abstract class A implements I{} class C extends A implements I{void m(){}} new C(); new A(){void m(){}};");

		// fields
		assertFailCompile("interface I{int c;}");
		assertSuccessSerialize("interface I{int c = 1, d = 0;} assert I.c == 1;");
		assertFailCompile("interface I{static int c;}");
		assertSuccessSerialize("interface I{static int c = 1;} assert I.c == 1;");
		assertFailCompile("interface I{int c = 0;} I.c = 1;");
		assertFailCompile("interface I{ { System.println(\"\"); } }");
		assertFailCompile("interface I{ static { System.println(\"\"); } }");

		// default
		assertSuccessSerialize("interface I{default int get(){return 1;}} class C implements I{}; assert new C().get() == 1;");
		assertFailCompile("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{}; new C().get();");
		assertFailCompile("interface I1{int get();} interface I2{int get();} class C implements I1, I2{}; new C().get();");
		assertSuccessSerialize("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{int get(){return 3;}}; assert new C().get() == 3;");
		assertSuccessSerialize("interface I1{int get();} interface I2{default int get(){return 2;}} class C implements I1, I2{}; assert new C().get() == 2;");

		// initializing order
		assertSuccessSerialize("interface A1{int a1 = 1; default int a(){return 0;} interface A2{int a2 = a1 + 1; default int a(){return 0;} interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 1; assert A1.A2.a2 == 2; assert A1.A2.A3.a3 == 3;");
		assertSuccessSerialize("interface A1{int a1 = A2.A3.a3 + 1; interface A2{int a2 = a1 + 1; interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 3; assert A1.A2.a2 == 1; assert A1.A2.A3.a3 == 2;");
		assertSuccessSerialize("interface A1{int a1 = A2.a2 + 1; interface A2{int a2 = A3.a3 + 1; interface A3{int a3 = a2 + 1;}}} assert A1.a1 == 3; assert A1.A2.a2 == 2; assert A1.A2.A3.a3 == 1;");

		assertFailCompile("final interface A{}");
		assertFailCompile("class A{void m(){static class B{}}}");

		// invalid format
		assertFailCompile("interface {}");
		assertFailCompile("interface 1A{}");
		assertFailCompile("interface A extends");
		assertFailCompile("interface A extends {}");
		assertFailCompile("interface A extends B, {}");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class C{int get(int... n){return n.length;}} assert new C().get(1, 2, 3) == 3;");
		assertSuccessSerialize("class O{} class O1 extends O{} class C{int get(O... n){return n.length;}} assert new C().get(new O1(), new O()) == 2;");
		assertSuccessSerialize("class O{} class C{int get(Object... n){return n.length;}} assert new C().get(new O(), \"x\", new Object()) == 3;");
		assertSuccessSerialize("class A{int get(){return 1;}} class B extends A{int get(){return super.get() + 1;}} assert new A().get() == 1; assert new B().get() == 2; ");

		assertFailCompile("class A{} class C extends A{void get(){}} A a = new C(); a.get();");
		assertSuccessSerialize("abstract class A{abstract void get();} class C extends A{void get(){}} A a = new C(); a.get();");

		// signature
		assertSuccessSerialize("class A{String m(int x, String arg){return \"\";}} String s = new A().m(0, null);");
		assertFailCompile("class A{int m(int x, String arg){return 1;}} String s = new A().m(0, null);");
		assertFailCompile("class A{Object m(int x, String arg){return null;}} class C{} C s = new A().m(0, null);");
		assertFailCompile("class C{void get(String x){}} new C().get(1);");

		assertFailCompile("class C{void get(byte x){}} new C().get((short)1);");
		assertFailCompile("class C{void get(byte x){}} new C().get('1');");
		assertFailCompile("class C{void get(byte x){}} new C().get(1);");
		assertFailCompile("class C{void get(byte x){}} new C().get(1l);");
		assertFailCompile("class C{void get(byte x){}} new C().get(1.0f);");
		assertFailCompile("class C{void get(byte x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(short x){}} new C().get('1');");
		assertFailCompile("class C{void get(short x){}} new C().get(1);");
		assertFailCompile("class C{void get(short x){}} new C().get(1l);");
		assertFailCompile("class C{void get(short x){}} new C().get(1.0f);");
		assertFailCompile("class C{void get(short x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(char x){}} new C().get((byte)1);");
		assertFailCompile("class C{void get(char x){}} new C().get((short)1);");
		assertFailCompile("class C{void get(char x){}} new C().get(1);");
		assertFailCompile("class C{void get(char x){}} new C().get(1l);");
		assertFailCompile("class C{void get(char x){}} new C().get(1.0f);");
		assertFailCompile("class C{void get(char x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(int x){}} new C().get(1l);");
		assertFailCompile("class C{void get(int x){}} new C().get(1.0f);");
		assertFailCompile("class C{void get(int x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(long x){}} new C().get(1.0f);");
		assertFailCompile("class C{void get(long x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(float x){}} new C().get(1.0d);");

		assertFailCompile("class C{void get(int x, byte y, char z){}} new C().get(1, 1, 1);");

		assertSuccessSerialize("class A{void m(int x, int y){}} class B extends A{void m(int x, byte y){}} new B().m(1, 1);");

		assertFailCompile("class A{void get(){}} class B extends A{int get(){}};"); // rewrite method with another return type

		assertSuccessSerialize("class C{void m(int... x){assert false;} void m(int x1, int x2, int x3){}} new C().m(1, 2, 3);");

		// TODO check incompatible throws of rewrite method
		assertSuccessSerialize("class E1 extends Exception{}; class E2 extends Exception{}; class A{void m() throws E1, E2 {throw new E1();}}");
		assertFailCompile("class E1 extends Exception{}; class E2 extends Exception{}; class A{void m() throws E1 {throw new E2();}}");
		assertFailCompile("class E extends Exception{};class A{void m() throws E {throw new E(); throw new E();}}");

		assertFailCompile("class A{void ()}");
		assertFailCompile("class A{void m()}");
		assertFailCompile("class A{void m(){}");
		assertFailCompile("class A{m(){}}");
		assertFailCompile("class A{void m(){return 1;}}");
		assertFailCompile("class A{int m(){return;}}");
		assertFailCompile("class A{int m(){return 1;return 1;}}");
		assertFailCompile("class A{void m(){return;return;}}");
		assertFailCompile("class A{void m() throws {}}");
		assertFailCompile("class A{void m() throws Exception, {}}");
	}

	@Test
	public void testConstructors() {
		// signature
		assertSuccessSerialize("class C{C(){}} new C();");
		assertSuccessSerialize("class A{A(int x, String arg){}} new A(0, \"a\"); new A(0, null);");

		assertFailCompile("class C{C(byte x){}} new C((short)1);");
		assertFailCompile("class C{C(byte x){}} new C('1');");
		assertFailCompile("class C{C(byte x){}} new C(1);");
		assertFailCompile("class C{C(byte x){}} new C(1l);");
		assertFailCompile("class C{C(byte x){}} new C(1.0f);");
		assertFailCompile("class C{C(byte x){}} new C(1.0d);");

		assertFailCompile("class C{C(short x){}} new C('1');");
		assertFailCompile("class C{C(short x){}} new C(1);");
		assertFailCompile("class C{C(short x){}} new C(1l);");
		assertFailCompile("class C{C(short x){}} new C(1.0f);");
		assertFailCompile("class C{C(short x){}} new C(1.0d);");

		assertFailCompile("class C{C(char x){}} new C((byte)1);");
		assertFailCompile("class C{C(char x){}} new C((short)1);");
		assertFailCompile("class C{C(char x){}} new C(1);");
		assertFailCompile("class C{C(char x){}} new C(1l);");
		assertFailCompile("class C{C(char x){}} new C(1.0f);");
		assertFailCompile("class C{C(char x){}} new C(1.0d);");

		assertFailCompile("class C{C(int x){}} new C(1l);");
		assertFailCompile("class C{C(int x){}} new C(1.0f);");
		assertFailCompile("class C{C(int x){}} new C(1.0d);");

		assertFailCompile("class C{C(long x){}} new C(1.0f);");
		assertFailCompile("class C{C(long x){}} new C(1.0d);");

		assertFailCompile("class C{C(float x){}} new C(1.0d);");

		assertFailCompile("class C{C(int x, byte y, char z){}} new C(1, 1, 1);");

		// varargs
		assertSuccessSerialize("class C{C(int length, int... n){assert n.length == length;}} new C(0); new C(3, 1, 2, 3);");
		assertSuccessSerialize("class O{} class O1 extends O{} class C{C(O... n){assert n.length == 2;}} new C(new O1(), new O());");
		assertSuccessSerialize("class O{} class C{Object[] o; C(Object... o){this.o = o;}} assert new C().o.length == 0; assert new C(new O(), \"x\", new Object()).o.length == 3;;");
		assertFailCompile("class C{C(String... s){}} new C(1, 2, 3);");
		assertFailCompile("class C{C(int... x, String s){}} new C(1, 2, 3, \"x\");");
		assertFailCompile("class C{C(int... x, int... y){}} new C(/*x=*/1, 2, /*y=*/3);");
		assertFailCompile("class C{C(byte... x){}} new C(1, 2, 3);");
		assertSuccessSerialize("class C{C(int... x){assert false;} C(int x1, int x2, int x3){}} new C(1, 2, 3);");

		// this
		assertSuccessSerialize("class A{A(int x){assert x == 1;} A(int x, int y){this(x); assert y == 2;} } new A(1, 2);");
		assertSuccessSerialize("class A{A(int x){assert x == 1;} A(int x, int y){this(x); assert y == 2;} } new A(1, 2);");
		assertFailCompile("class A{A(){this();}} new A();");
		assertFailSerialize("class A{A(int x){this(true);} A(boolean x){this(1);}} new A(1);"); // recursive constructor invocation
		assertFailCompile("class A{A(){this(1);} A(byte x){}} new A();");

		// super
		assertSuccessSerialize("class A{A(int x){this.x = x;} int x;} class B extends A{B(int y){super(y);}} A a = new B(1); assert a.x == 1;");
		assertSuccessSerialize("class A{A(int x, int y){}} class B extends A{B(int x, byte y){super(x, y);}} new B(1, (byte)1);");
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
		assertFailCompile("enum {}");
		assertFailCompile("enum E{");
		assertFailCompile("enum E{1}");
		assertFailCompile("enum E{e(}");
		assertFailCompile("enum E{e(1)}");
		assertFailCompile("enum E{e(1,) E(int x, int y){}}");
		assertFailCompile("enum E{e(1,); E(int x, int y){}}");
		assertFailCompile("enum E{e(1,true) E(int x, int y){}}");
		assertFailCompile("enum E{e(); E(int a){}}");
		assertFailCompile("enum E{e(true); E(int a){}}");
		assertFailCompile("enum E1{e1} enum E2 extends E1{e2}");
		assertFailCompile("enum E1{e1} new E1();");
		assertFailCompile("enum E1{e1} E1 e = new Object();");
		assertFailCompile("enum E1{A, b, C, A, d, C, e, f, g}");
	}

	@Test
	public void testRecords() {
		assertSuccessSerialize("record Rec(int a); Rec r = new Rec(1);");
		assertFailCompile("record Rec(int a, long a);");
		assertFailCompile("record (int a);");
		assertSuccessSerialize("class A{}; record Rec(A a, A b); new Rec(new A(), new A());");

		assertSuccessSerialize("record Rec(int a, String field); Rec rec = new Rec(1, \"abc\"); assert rec.getA() == 1; assert rec.getField().equals(\"abc\");");
		assertSuccessSerialize("record Rec(int a, int b){Rec(int a){this.a = a; b = 2;}}; Rec rec1 = new Rec(1, 2); Rec rec2 = new Rec(1); assert rec2.getA() == 1; assert rec2.getB() == 2;");
		assertFailCompile("record Rec();");
		assertFailCompile("record Rec(){int a;};");

		// equals
		assertSuccessSerialize("record Rec(int a, String b); assert new Rec(1, \"a\").equals(new Rec(1, \"a\"));");
		assertFailSerialize("record Rec(int a, String b); assert new Rec(1, \"a\").equals(new Rec(2, \"a\"));");
		assertSuccessSerialize("record Rec(int a, String b); assert new Rec(1, \"abc\").equals(new Rec(1, \"abc\"));");
		assertSuccessSerialize("class O{} O o = new O(); record Rec(O o); assert new Rec(o).equals(new Rec(o));");
		assertFailSerialize("class O{} record Rec(O o); assert new Rec(new O()).equals(new Rec(new O()));");
		assertSuccessSerialize("class O{int a; O(int a){this.a = a;} boolean equals(Object o){return a == ((O)o).a;}} record Rec(O o); assert new Rec(new O(1)).equals(new Rec(new O(1)));");

		// hashcode
		assertSuccessSerialize("class O{} record Rec(O o, int x, String a); new Rec(new O(), 1, \"x\").hashCode();");

		// instanceof
		assertSuccessSerialize("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(int a, String b) rec){assert a == 1; a = 2; assert b.equals(\"abc\"); assert rec.getA() == 2; assert rec.getB().equals(\"abc\");}");
		assertSuccessSerialize("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(long a, Object b) rec){assert a == 1; a = 2; assert b.equals(\"abc\"); assert rec.getA() == 2; assert rec.getB().equals(\"abc\");}");
		assertFailCompile("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(byte a) rec);");
		assertFailCompile("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(String b, String a) rec);");
		assertFailCompile("record Rec(int a, String b, int c); Object r = new Rec(1, \"abc\", 2); if(r instanceof Rec(int _a, String b) rec);");

		// switch-case
		assertSuccessSerialize("record Rec(int a); Object o = new Rec(1); switch(o){case \"o\": assert false; break; case Rec(int a) r: assert a == 1; a = 2; assert r.getA() == 2; break;} assert ((Rec)o).getA() == 2;");
		assertSuccessSerialize("record Rec(int a); Object o = new Rec(1); switch(o){case \"o\": assert false; break; case Rec(int a) r when a == 1: assert a == 1; a = 2; assert r.getA() == 2; break;} assert ((Rec)o).getA() == 2;");
		// Duplicated local variable a
		assertFailCompile("record Rec(int a); Object o = new Rec(1); boolean a = true; switch(o){case \"o\": assert false; break; case Rec(int a) r when a == 1: assert a == 1; a = 2; assert r.getA() == 2; break;} assert ((Rec)o).getA() == 2;");
		assertFailCompile("record R1(int x); record R2(int x) extends R1;");
	}
}