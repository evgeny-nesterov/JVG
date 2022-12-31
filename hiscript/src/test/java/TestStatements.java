import org.junit.jupiter.api.Test;

public class TestStatements extends HiTest {
	@Test
	public void testDeclaration() {
		assertSuccessSerialize("int a;");
		assertSuccessSerialize("int a123;");
		assertSuccessSerialize("int _a;");
		assertSuccessSerialize("int _;");
		assertSuccessSerialize("int _0123;");
		assertSuccessSerialize("int _a;");
		assertSuccessSerialize("int $;");
		assertSuccessSerialize("int _$;");
		assertSuccessSerialize("int $a__;");
		assertSuccessSerialize("int ____$a_$b_$c___;");
		assertSuccessSerialize("int переменная;");

		assertSuccessSerialize("int a = 1; assert a == 1;");
		assertSuccessSerialize("String a, b, c;");
		assertSuccessSerialize("String a = \"a\", b, c = null; assert \"a\".equals(a); assert c == null;");
		assertSuccessSerialize("{int a = 1;} int a = 2; assert a == 2;");

		assertFailCompile("String 0var;");
		assertFailCompile("String a, a;");
		assertFailCompile("int a = 1; String a = \"\";");
		assertFailCompile("int x; int y = x;");
		assertFailCompile("int x = 1; int y = true || x;");
		assertFailCompile("int x = 1, y = 0.0;");
	}

	@Test
	public void testFor() {
		assertSuccessSerialize("int j = 0; for(int i = 0; i < 10; i++) {assert i == j; j++;}");
		assertSuccessSerialize("int i = 0; for(; i < 10; i++); assert i == 10;");
		assertSuccessSerialize("int x = 0; for(int i = 0, j = 10; i < 10 && j >= 0; i++, j--) {x++;} assert x == 10;");

		// iterable for
		assertSuccessSerialize("int x[] = {0, 1, 2, 3}; for (int i : x) {i++;}; for (int i : x); for (int i : x) break; for (int i : x) {continue;}");
		assertSuccessSerialize("String[] x = {\"a\", \"b\", \"c\"}; for (String i : x) {i += i;};");
		assertSuccessSerialize("int[] arr = {1, 2, 3}; int i = 0; for (int x : arr) assert x == arr[i++];");
		assertSuccessSerialize("ArrayList l = new ArrayList(); l.add(\"a\"); l.add(\"b\"); l.add(\"c\"); int i = 0; for (String s : l) {assert s.equals(l.get(i++));}");

		assertSuccessSerialize("for(;;) {break;}");
		assertSuccessSerialize("for(int i = 0;;) {break;}");
		assertSuccessSerialize("int i = 0; for(;i < 0;); assert i == 0;");
		assertSuccessSerialize("int i = 0; for(;;i++, i++) {break;} assert i == 0;");
		assertSuccessSerialize("int i = 0; for(;;i++, i++) {{{break;}}} assert i == 0;");
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
	public void testDoWhile() {
		assertSuccessSerialize("int x = 3; do{x--;} while(x != 0); assert x == 0;");
	}

	@Test
	public void testIf() {
		assertSuccessSerialize("int x = 0; if (x == 0) x++; assert x == 1;");
		assertSuccessSerialize("if (false) assert false;");
		assertSuccessSerialize("if (true);");
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
		assertSuccessSerialize("class O{int a; O(int a){this.a=a;}} switch(new O(1)){case O o when o.a == 0: assert false : \"case 1\"; case o.a == 1: assert o.a == 1;return;} assert false : \"case 2\";");
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
		assertSuccessSerialize("class A implements AutoCloseable{int x = 1; public void close(){x--;}} A a_; try(A a = a_= new A()) {assert a.x==1;} finally{assert a_.x==0;} assert a_.x==0;");
	}

	@Test
	public void testNew() {
		assertSuccessSerialize("new Object().toString();");
		assertSuccessSerialize("assert new Object(){String toString(){return \"a\";}}.toString().equals(\"a\");");
		assertSuccessSerialize("assert (new int[]{1})[0] == 1;");
		assertSuccessSerialize("class A{} A a = new A(); assert a instanceof A;");
		assertSuccessSerialize("class A{A(int x){}} new A(1);");
		assertSuccessSerialize("assert (new int[1])[0] == 0;");
		assertSuccessSerialize("assert (new int[]{1,2,3})[2] == 3;");
		assertSuccessSerialize("assert (new int[1][1])[0][0] == 0;");
		assertSuccessSerialize("assert new int[][]{{1,2},{3,4}}[1][1] == 4;");
	}

	@Test
	public void testBreakContinueLabel() {
		assertSuccessSerialize("A:{if(true) break A; assert false;};");
		assertSuccessSerialize("FOR: for(;;) {int a = 1; switch(a){case 0: break; case 1: break FOR;} assert false;}");
		assertSuccessSerialize("for (int i = 0; i < 10; i++) {if(i<10) continue; assert false;}");
		assertSuccessSerialize("for (int i = 0; i < 10; i++) BLOCK: {if(i<10) continue BLOCK; assert false;}");
	}

	@Test
	public void testReturn() {
		// methods
		assertSuccessSerialize("class A{int m(){return 1;}} assert new A().m() == 1;");
		assertSuccessSerialize("class A{String m(){return \"1\";}} assert new A().m().equals(\"1\");");
		assertFailCompile("class A{int m(){return \"\";}}");
		assertFailCompile("class A{int m(){return;}}");
		assertFailCompile("class A{void m(){return; int x = 1;}}");
		assertFailCompile("class A{void m(){return; return;}}");

		assertSuccessSerialize("class A{void m(){return;}} new A().m();");
		assertFailCompile("class A{void m(){return;}} Object x = new A().m();");
		assertFailCompile("class A{void m(){return 1;}}");

		assertSuccessSerialize("class A{int m(){ if(true) {{{return 1;}}} else return 2; }} assert new A().m() == 1;");

		// constructors
		assertSuccessSerialize("class A{A(){return;}}");
		assertFailCompile("class A{A(){return \"\";}}");
		assertFailCompile("class A{A(){return; int x = 1;}}");
		assertFailCompile("class A{A(){return; return;}}");

		assertSuccessSerialize("class A{int x = 0; A(){if(true) return; x = 1;}} assert new A().x == 0;");

		// initializers
		assertSuccessSerialize("class A{{int x = 0; return;}}");
		assertSuccessSerialize("class A{static{int x = 0; return;}}");
		assertFailCompile("class A{{return \"\";}}");
		assertFailCompile("class A{{return; int x = 0;}}");
		assertFailCompile("class A{static{return; return;}}");

		assertSuccessSerialize("class A{int x = 0; {if(true) return; x = 1;}} assert new A().x == 0;");
	}
}