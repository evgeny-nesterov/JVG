import org.junit.jupiter.api.Test;

public class TestStatements extends HiTest {
	@Test
	public void testFor() {
		assertSuccessSerialize("int j = 0; for(int i = 0; i < 10; i++) {assert i == j; j++;}");
		assertSuccessSerialize("int i = 0; for(; i < 10; i++); assert i == 10;");
		assertSuccessSerialize("for(int i = 0, j = 10; i < 10 && j >= 0; i++, j--) {}");
		assertSuccessSerialize("int x[] = {0, 1, 2, 3}; for (int i : x) {}; for (int i : x); for (int i : x) break; for (int i : x) {continue;}");
		assertSuccessSerialize("String[] x = {\"a\", \"b\", \"c\"}; for (String i : x) {i += i;};");

		assertSuccessSerialize("for(;;) {break;}");
		assertSuccessSerialize("for(int i = 0;;) {break;}");
		assertSuccessSerialize("int i = 0; for(;i < 0;);");
		assertSuccessSerialize("int i = 0; for(;;i++, i++) {break;}");
	}

	@Test
	public void testWhile() {
		assertSuccessSerialize("int x = 3; while(x != 0) {x--;}");
		assertSuccessSerialize("int x = 3; while(true) {x--; if(x <= 1) break; else continue; x = 1;} assert x == 1;");
		assertSuccessSerialize("int x = 3; do {x--;} while(x != 0); assert x == 0;");
		assertSuccessSerialize("int x = 3; do {x--; if(x == 1) break;} while(x != 0); assert x == 1;");
		assertSuccessSerialize("int x = 3; do {x--; if(x >= 1) continue; break;} while(true); assert x == 0;");
	}

	@Test
	public void testIf() {
		assertSuccessSerialize("int x = 0; if (x == 0) x++; assert x == 1;");
		assertSuccessSerialize("int x = 0; if (x != 0) {x++;} assert x == 0;");
		assertSuccessSerialize("int x = 0; if (x != 0) {x = -1;} else {x = 1;} assert x == 1;");
		assertSuccessSerialize("int x = 2; if (x == 0) {assert false;} else if(x == 1) {assert false;} else x++; assert x == 3;");
		assertSuccessSerialize("int x = 1; if (x > 0) if(x != 0) if (x == 1) x++; assert x == 2;");
		assertSuccessSerialize("String s = \"a\"; if (s != null && s.length() == 1 && s.equals(\"a\")) {s = null;} assert s == null;");
	}

	@Test
	public void testSwitch() {
		assertSuccessSerialize("int x = 2; switch(x) {case 0: x=0; break; case 1: x--; break; case 2, 3, x + 1: x++; break;} assert x == 3;");
		assertSuccessSerialize("int x = 1; switch(x) {case 0: x=0; case 1: x++; case 2: x++;} assert x == 3;");
		assertSuccessSerialize("int x = 4; switch(x) {case 0: x=0; break; case 1: x--; break; case 2: x++; break; default: x += 2;} assert x == 6;");
		assertSuccessSerialize("String s = \"c\"; switch(s) {case \"a\", \"x\": s = \"\"; break; case \"b\": break; case \"c\", \"b\": s += 1; break;} assert s.equals(\"c1\");");
		assertSuccessSerialize("class A {}; class B{}; A a = new A(); Object b = new B(); switch(b) {case a: break; case new A(): break; case b: b = a;} assert b == a && b.equals(a);");
	}

	@Test
	public void testExceptions() {
		assertSuccessSerialize("new Exception(); new Exception(\"error\"); new RuntimeException(); new RuntimeException(\"error\");");
		assertSuccessSerialize("class E extends Exception {E(){} E(String msg){super(msg);}}; E e1 = new E(); E e2 = new E(\"error\"); assert \"error\".equals(e2.getMessage()); assert e1 instanceof Exception;");
		assertFailSerialize("throw new Exception(\"error\");");
		assertFailSerialize("throw new Object();");
		assertFailSerialize("class E extends Exception{} throw new E();");
		assertSuccessSerialize("try {throw new Exception(\"error\");} catch(Exception e) {assert e.getMessage().equals(\"error\");} ");
		assertSuccessSerialize("class E extends Exception {E(String message){super(message);}} try {throw new E(\"error\");} catch(E e) {assert e.getMessage().equals(\"error\");} ");
		assertFailSerialize("class E extends Exception {} try {throw new Exception();} catch(E e) {}");
		assertSuccessSerialize("class E extends Exception {} try {throw new Exception();} catch(E e) {assert false;} catch(RuntimeException e) {assert false;} catch(Exception e){}");
		assertSuccessSerialize("class E extends Exception {E(String message){super(message);}} " + //
				"class A {void m(int x) throws E {if (x == 1) throw new E(\"error-\" + x);}}" + //
				"try {A a = new A(); a.m(1);} catch(E e) {assert e.getMessage().equals(\"error-1\");}");
	}

	@Test
	public void testNew() {
		assertSuccessSerialize("new Object().toString();");
	}

	@Test
	public void testClasses() {
		assertSuccessSerialize("class C{}");
		assertSuccessSerialize("class C{int c;} assert new C().c == 0;");
		assertSuccessSerialize("class C{int c = 1;} assert new C().c == 1;");
		assertSuccessSerialize("class C{static int c;} assert C.c == 0;");
		assertSuccessSerialize("class C{static int c = 1;} assert C.c == 1;");
	}

	@Test
	public void testInnerClasses() {
		// static inner class
		assertSuccessSerialize("class A{static class B{}} A.B b = new A.B(); assert b instanceof A.B;");
		assertSuccessSerialize("static class A{static class B{B(int i){}}} A.B b = new A.B(0); assert b instanceof A.B;");
		assertSuccessSerialize("class A{static class B{static class C{static class D{}}}} A.B.C.D d = new A.B.C.D(); assert d instanceof A.B.C.D;");
		assertFailSerialize("class A{static class B{}} A a = new A(); A.B b = a.new B();");
		assertFailSerialize("class A{static class B{}} B b;");
		assertFailSerialize("class A{static class B{}} A.B b = new B();");
		assertSuccessSerialize("class A {void get(){}} A a = new A(); a.get();");

		// not static inner class
		assertSuccessSerialize("class A{class B{}} A a = new A(); A.B b = a.new B(); new A().new B();");
		assertSuccessSerialize("class A{static class B{class C{C(int i){}}}} A.B b = new A.B(); A.B.C c = b.new C(0); new A.B().new C(0);");
		assertFailSerialize("class A{class B{}} A.B b = new A.B();");
		assertSuccessSerialize("class A{class B{class C{int get(){return 123;}} C c = new C();} B b = new B();} A a = new A(); assert a.b != null; assert a.b.c != null; assert a.b.c.get() == 123;");
		assertSuccessSerialize("class A{class B{} B b = new B();} A a = new A(); assert a.b instanceof A.B;");
	}

	@Test
	public void testInterfaces() {
		// extends, implements
		assertSuccessSerialize("interface I{} class C implements I{} assert new C() instanceof C; assert new C() instanceof I;");
		assertFailSerialize("interface I{} new I();");
		assertSuccessSerialize("interface I1{} interface I2 extends I1{} class C implements I2{} I1 c = new C(); c instanceof C; c instanceof I1; c instanceof I2;");
		assertSuccessSerialize("interface I1{} interface I2{} interface I3 extends I1{} interface I12 extends I1, I1{} class C implements I12, I3{} Object c = new C(); c instanceof C; c instanceof I1; c instanceof I2; c instanceof I3; c instanceof I12;");

		// fields
		assertFailSerialize("interface I{int c;}");
		assertSuccessSerialize("interface I{int c = 1, d = 0;} assert I.c == 1;");
		assertFailSerialize("interface I{static int c;}");
		assertSuccessSerialize("interface I{static int c = 1;} assert I.c == 1;");
		assertFailSerialize("interface I{int c = 0;} I.c = 1;");
		assertFailSerialize("interface I{ { System.println(\"\"); } }");
		assertFailSerialize("interface I{ static { System.println(\"\"); } }");

		// default
		assertSuccessSerialize("interface I{default int get(){return 1;}} class C implements I{}; assert new C().get() == 1;");
		assertFailSerialize("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{}; new C().get();");
		assertFailSerialize("interface I1{int get();} interface I2{int get();} class C implements I1, I2{}; new C().get();");
		assertSuccessSerialize("interface I1{default int get(){return 1;}} interface I2{default int get(){return 2;}} class C implements I1, I2{int get(){return 3;}}; assert new C().get() == 3;");
		assertSuccessSerialize("interface I1{int get();} interface I2{default int get(){return 2;}} class C implements I1, I2{}; assert new C().get() == 2;");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class C{int get(int... n){return n.length;}} assert new C().get(1, 2, 3) == 3;");
		assertSuccessSerialize("class O{} class O1 extends O{} class C{int get(O... n){return n.length;}} assert new C().get(new O1(), new O()) == 2;");
		assertSuccessSerialize("class O{} class C{int get(Object... n){return n.length;}} assert new C().get(new O(), \"x\", new Object()) == 3;");
		assertSuccessSerialize("class A{int get(){return 1;}} class B extends A{int get(){return super.get() + 1;}} assert new A().get() == 1; assert new B().get() == 2; ");

		// null value
		assertSuccessSerialize("class A{String a(String arg){}} new A().a(null); ");
	}

	@Test
	public void testEnums() {
	}

	@Test
	public void testRecords() {
		assertSuccessSerialize("record Rec(int a); new Rec(1);");
		assertFailSerialize("record Rec(int a, long a);");
		assertSuccessSerialize("class A{}; record Rec(A a, A b); new Rec(new A(), new A());");

		assertSuccessSerialize("record Rec(int a, String field); Rec rec = new Rec(1, \"abc\"); assert rec.getA() == 1; assert rec.getField().equals(\"abc\");");
		assertSuccessSerialize("record Rec(int a, int b){Rec(int a){this.a = a; b = 2;}}; Rec rec1 = new Rec(1, 2); Rec rec2 = new Rec(1); assert rec2.getA() == 1; assert rec2.getB() == 2;");
		assertFailSerialize("record Rec();");
		assertFailSerialize("record Rec(){int a;};");
	}
}