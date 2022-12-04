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
	}

	@Test
	public void testNew() {
		assertSuccessSerialize("new Object().toString();");
		assertSuccessSerialize("assert new Object(){String toString(){return \"a\";}}.toString().equals(\"a\");");
		assertSuccessSerialize("assert (new int[]{1})[0] == 1;");
		// FAIL assertSuccessSerialize("assert (new int[1])[0] == 0;");
	}
}