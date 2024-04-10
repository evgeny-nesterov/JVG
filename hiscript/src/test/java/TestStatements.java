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
		assertSuccessSerialize("for(long x : new int[]{1, 2, 3}){}");
		assertSuccessSerialize("int x = 0; for(int y : new int[]{1, 2, 3}){x=y; break;} assert x == 1;");

		assertFailCompile("for(private int x = 0; x < 10; x++){}");
		assertFailCompile("for(int x : new String[]{\"a\", \"b\"}){}");
		assertFailCompile("for(int x : new long[]{1L, 2L, 3L}){}");

		assertFailCompile("for() {}");
		assertFailCompile("for(;) {}");
		assertFailCompile("for(;;;) {}");
		assertFailCompile("for(;;) {");
		assertFailCompile("for(;;)");
		assertFailCompile("int x = 0; for(;x=1, x=2;) {}");
		assertFailCompile("for(int x : new Object()) {}");
	}

	@Test
	public void testWhile() {
		assertSuccessSerialize("int x = 3; while(x != 0) {x--;}");
		assertSuccessSerialize("int x = 3; while(true) {x--; if(x <= 1) break; else continue; x = 1;} assert x == 1;");

		assertFailCompile("while() {}");
		assertFailCompile("while(1) {}");
		assertFailCompile("while(1.1) {}");
		assertFailCompile("while(\"\") {}");
		assertFailCompile("while(true;) {}");
		assertFailCompile("while(true)");
		assertFailCompile("while(true) {");
	}

	@Test
	public void testDoWhile() {
		assertSuccessSerialize("int x = 3; do{x--;} while(x != 0); assert x == 0;");
		assertSuccessSerialize("int x = 3; do {x--;} while(x != 0); assert x == 0;");
		assertSuccessSerialize("int x = 3; do {x--; if(x == 1) break;} while(x != 0); assert x == 1;");
		assertSuccessSerialize("int x = 3; do {x--; if(x >= 1) continue; break;} while(true); assert x == 0;");

		assertFailCompile("do {} do(true);");
		assertFailCompile("do {} while();");
		assertFailCompile("do {} while(true;);");
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

		assertFailCompile("if() {}");
		assertFailCompile("if(true;) {}");
		assertFailCompile("if(true)");
		assertFailCompile("if(1) {}");
		assertFailCompile("if(1.0) {}");
		assertFailCompile("if(\"\") {}");
		assertFailCompile("if(true) {} else if(){}");
		assertFailCompile("if(true) {} else if(false)");
		assertFailCompile("if(true) {} else if(false){");
		assertFailCompile("if(true) {} else if(false){} else ");
		assertFailCompile("if(true) {} else if(false){} else {");
	}

	@Test
	public void testSwitch() {
		assertSuccessSerialize("switch(1){}");
		assertSuccessSerialize("int x = 1; switch(x){case 1: assert true;break; case 2: assert false;break;}");
		assertSuccessSerialize("int x = 1; switch(x){case 2: assert false;break; default: assert true;break;}");
		assertSuccessSerialize("int x = 1; switch(x){case 2, x == 1: assert true;break; case 1, x == 2: assert false;break;}");
		assertSuccessSerialize("int x = 2; switch(x) {case 0: x=0; break; case 1: x--; break; case 2, 3, x + 1: x++; break;} assert x == 3;");
		assertSuccessSerialize("int x = 1; switch(x) {case 0: x=0; case 1: x++; case 2: x++;} assert x == 3;");
		assertSuccessSerialize("int x = 4; switch(x) {case 0: x=0; break; case 1: x--; break; case 2: x++; break; default: x += 2;} assert x == 6;");
		assertSuccessSerialize("String s = \"c\"; switch(s) {case \"a\", \"x\": s = \"\"; break; case \"b\": break; case \"c\", \"b\": s += 1; break;} assert s.equals(\"c1\");");
		assertSuccessSerialize("class A {}; class B{}; A a = new A(); Object b = new B(); switch(b) {case a: break; case new A(): break; case b: b = a;} assert b == a && b.equals(a);");
		assertSuccessSerialize("class O{int a; O(int a){this.a=a;}} switch(new O(1)){case O o when o.a == 0: assert false : \"case 1\"; case o.a == 1: assert o.a == 1;return;} assert false : \"case 2\";");
		assertFailCompile("int x = 1; switch(x){case 1:break; case \"\":break;}");
		assertSuccessSerialize("Object s = null; switch(s){case \"a\": assert true;break; case \"b\": assert false;break; case null: assert s == null;break;}");
		assertFailCompile("int x = 1; switch(x){case 1,:break;}");

		assertFailCompile("switch(){}");
		assertFailCompile("switch(1);");
		assertFailCompile("switch(1;){}");
		assertFailCompile("switch(1){case}");
		assertFailCompile("switch(1){case :}");
		assertFailCompile("switch(1){case 1: break}");
		assertFailCompile("switch(1){case 1: break; default}");
	}

	@Test
	public void testExceptions() {
		assertSuccessSerialize("new Exception(); new Exception(\"error\"); new RuntimeException(); new RuntimeException(\"error\");");
		assertSuccessSerialize("class E extends Exception {E(){} E(String msg){super(msg);}}; E e1 = new E(); E e2 = new E(\"error\"); assert \"error\".equals(e2.getMessage()); assert e1 instanceof Exception;");

		// throw in root
		assertFailCompile("throw new Exception(\"error\");"); // unreported exception
		assertFailCompile("throw new Exception(1, 2, 3);");
		assertFailSerialize("throw new RuntimeException(\"error\");");
		assertFailCompile("throw new Object();");
		assertFailCompile("class E extends Exception{} throw new E();"); // unreported exception
		assertFailSerialize("class E extends RuntimeException{} throw new E();");

		// throw in try
		assertSuccessSerialize("try{throw new Exception(\"error\");} catch(Exception e){assert e.getMessage().equals(\"error\");} ");
		assertSuccessSerialize("class E extends Exception{E(String message){super(message);}} try {throw new E(\"error\");} catch(E e) {assert e.getMessage().equals(\"error\");} ");
		assertFailCompile("class E extends Exception{} try{throw new Exception();} catch(E e) {}"); // unreported exception
		assertFailSerialize("class E extends RuntimeException{} try{throw new RuntimeException();} catch(E e) {}");
		assertSuccessSerialize("class E extends Exception{} try{throw new Exception();} catch(E e){assert false;} catch(RuntimeException e){assert false;} catch(Exception e){}");
		assertSuccessSerialize("class E extends Exception {E(String message){super(message);}} " + //
				"class A {void m(int x) throws E {if (x == 1) throw new E(\"error-\" + x);}}" + //
				"try {A a = new A(); a.m(1);} catch(E e) {assert e.getMessage().equals(\"error-1\");}");
		assertSuccessSerialize("class A implements AutoCloseable{int x = 1; public void close(){x--;}} A a_; try(A a = a_= new A()) {assert a.x==1;} finally{assert a_.x==0;} assert a_.x==0;");
		assertSuccessSerialize("class A implements AutoCloseable{public void close(){}} try(A a1 = new A(); A a2 = new A()) {}");
		assertFailCompile("class A implements AutoCloseable{public void close(){}} try(A a = new A();) {}");
		assertSuccessSerialize("class E1 extends Exception{E1(){} E1(String msg){super(msg);}} class E2 extends Exception{E2(){} E2(String msg){super(msg);}} try{throw new E2(\"error\");} catch(E1 | E2 e){assert e.getMessage().equals(\"error\");}");
		assertFailSerialize("class A implements AutoCloseable{public void close(){throw new RuntimeException();}} try(A a = new A()) {} catch(RuntimeException e){}");

		// Exception has already been caught
		assertFailCompile("try{} catch(Exception e){} catch(Exception e){}"); // Exception 'java.lang.Exception' has already been caught
		assertFailCompile("try{} catch(Exception e){} catch(RuntimeException e){}"); // Exception 'java.lang.RuntimeException' has already been caught
		assertFailCompile("try{} catch(RuntimeException e){} catch(RuntimeException e){}");
		assertFailCompile("class E extends Exception{} try{} catch(E e){} catch(E e){}");
		assertFailCompile("class E extends Exception{} try{} catch(Exception e){} catch(E e){}");
		assertFailCompile("try{} catch(Exception | Exception e){}");
		assertFailCompile("try{} catch(Exception | RuntimeException e){}");
		assertFailCompile("try{} catch(RuntimeException | Exception e){}");

		// in constructor
		assertFailCompile("class A{A() {throw new Exception();}}");
		assertFailCompile("class A{A() throws RuntimeException {throw new Exception();}}");
		assertFailCompile("class A{A() throws Exception {throw new Exception(); super();}}");
		assertFail("class A{A(){throw new RuntimeException();}} new A();");

		// in method
		assertFailCompile("class A{void m() {throw new Exception();}}");
		assertFailCompile("class A{void m() throws RuntimeException {throw new Exception();}}");
		assertFail("class A{void m(){throw new RuntimeException();}} new A().m();");

		// in initialization
		assertFailCompile("class A{{throw new Exception();}}");
		assertFail("class A{{throw new RuntimeException();}} new A();");

		assertFailCompile("class A{static{throw new Exception();}}");
		assertSuccessSerialize("class A{static{throw new RuntimeException();}}");
		assertFail("class A{static int x = 1; static{throw new RuntimeException();}} int x = A.x;");
		assertFailCompile("try(int x = 1) {}");
		assertFailCompile("try(Object x = new Object()) {}");
		assertFailCompile("try {} catch(String | Integer e){}");
		assertFailCompile("try {} catch(RuntimeException | Integer e){}");

		// invalid format
		assertFailCompile("throw Exception();");
		assertFailCompile("throw new;");
		assertFailCompile("throw new Exception;");
		assertFailCompile("try()");
		assertFailCompile("try(){}");
		assertFailCompile("try{} catch(Exception ){}");
		assertFailCompile("try{} catch(Exception | e){}");
		assertFailCompile("try{} catch(Exception e){} finally");
		assertFailCompile("try(Object x) {}");
	}

	@Test
	public void testNew1() {
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
		assertSuccessSerialize("A:{break A;};");
		assertSuccessSerialize("A:{{{break A;}}};");
		assertSuccessSerialize("A:break A;");
		assertSuccessSerialize("A:{continue A;};");
		assertSuccessSerialize("A:{{{continue A;}}};");
		assertSuccessSerialize("A:continue A;");
		assertSuccessSerialize("A:B:C:D:{break B;}");
		assertFailCompile("break A;");
		assertFailCompile("A:{} break A;");
		assertFailCompile("A:{break B;}");
		assertFailCompile("A:{continue B;}");

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
		assertSuccessSerialize("class A{int x; A set(int x){this.x = x; return this;} int get(){return this.x;}} assert new A().set(123).get() == 123;");
		assertSuccessSerialize("class A{int x; A set(int y){x = y; return this;} int get(){return this.x;}} assert new A().set(123).get() == 123;");
		assertFailCompile("class A{int m(){return \"\";}}");
		assertFailCompile("class A{int m(){return 1L;}}");
		assertFailCompile("class A{int m(){return;}}");
		assertFailCompile("class A{void m(){return; int x = 1;}}");
		assertFailCompile("class A{void m(){return; return;}}");
		assertFailCompile("class A{void m(){return A;}}");

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

	@Test
	public void testThis() {
		assertSuccessSerialize("class A{{Object a = this; assert a instanceof A;}} new A();");
		assertSuccessSerialize("class A{void m(){Object a = A.this; assert a instanceof A;}} new A().m();");
		assertSuccessSerialize("class A{int x = 1; {this.x = 2;}} assert new A().x == 2;");
		assertSuccessSerialize("class A{int x = 1; {A.this.x = 2;}} assert new A().x == 2;");
		assertSuccessSerialize("class A{int x(){return 1;} int y(){return this.x() + 1;}} assert new A().y() == 2;");
		assertSuccessSerialize("class A{int x(){return 1;} int y(){return A.this.x() + 1;}} assert new A().y() == 2;");

		assertFailCompile("this();");
		assertFailCompile("this;");
		assertFailCompile("this = null;");
	}

	@Test
	public void testSuper() {
		assertSuccessSerialize("class A{int x = 1;} class B extends A{int x = 2; int get(){return super.x;}} assert new B().get() == 1;");
		assertSuccessSerialize("class A{int x(){return 1;}} class B extends A{int x(){return 2;} int get(){return super.x();}} assert new B().get() == 1;");

		assertFailCompile("class A{void m1(){} void m2(){super.m1();}}");

		assertFailCompile("super();");
		assertFailCompile("super;");
		assertFailCompile("super = null;");
	}

	@Test
	public void testFails() {
		assertFailCompile("class;");
		assertFailCompile("true;");
		assertFailCompile("1;");
		assertFailCompile("1+2;");
		assertFailCompile("(1);");
		assertFailCompile("(1+2);");
		assertFailCompile("{1}");
	}
}