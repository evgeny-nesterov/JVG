import org.junit.jupiter.api.Test;

public class TestStatements extends HiTest {
	@Test
	public void testFor() {
		assertSuccess("int j = 0; for(int i = 0; i < 10; i++) {assert i == j; j++;}");
		assertSuccess("int i = 0; for(; i < 10; i++); assert i == 10;");
		assertSuccess("for(int i = 0, j = 10; i < 10 && j >= 0; i++, j--) {}");
		assertSuccess("int x[] = {0, 1, 2, 3}; for (int i : x) {}; for (int i : x); for (int i : x) break; for (int i : x) {continue;}");
		assertSuccess("String[] x = {\"a\", \"b\", \"c\"}; for (String i : x) {i += i;};");
	}

	@Test
	public void testWhile() {
		assertSuccess("int x = 3; while(x != 0) {x--;}");
		assertSuccess("int x = 3; while(true) {x--; if(x <= 1) break; else continue; x = 1;} assert x == 1;");
		assertSuccess("int x = 3; do {x--;} while(x != 0); assert x == 0;");
		assertSuccess("int x = 3; do {x--; if(x == 1) break;} while(x != 0); assert x == 1;");
		assertSuccess("int x = 3; do {x--; if(x >= 1) continue; break;} while(true); assert x == 0;");
	}

	@Test
	public void testIf() {
		assertSuccess("int x = 0; if (x == 0) x++; assert x == 1;");
		assertSuccess("int x = 0; if (x != 0) {x++;} assert x == 0;");
		assertSuccess("int x = 0; if (x != 0) {x = -1;} else {x = 1;} assert x == 1;");
		assertSuccess("int x = 2; if (x == 0) {assert false;} else if(x == 1) {assert false;} else x++; assert x == 3;");
		assertSuccess("int x = 1; if (x > 0) if(x != 0) if (x == 1) x++; assert x == 2;");
		assertSuccess("String s = \"a\"; if (s != null && s.length() == 1 && s.equals(\"a\")) {s = null;} assert s == null;");
	}
	@Test
	public void testSwitch() {
		assertSuccess("int x = 2; switch(x) {case 0: x=0; break; case 1: x--; break; case 2, 3, x + 1: x++; break;} assert x == 3;");
		assertSuccess("int x = 1; switch(x) {case 0: x=0; case 1: x++; case 2: x++;} assert x == 3;");
		assertSuccess("int x = 4; switch(x) {case 0: x=0; break; case 1: x--; break; case 2: x++; break; default: x += 2;} assert x == 6;");
		assertSuccess("String s = \"c\"; switch(s) {case \"a\", \"x\": s = \"\"; break; case \"b\": break; case \"c\", \"b\": s += 1; break;} assert s.equals(\"c1\");");
		assertSuccess("class A {}; class B{}; A a = new A(); Object b = new B(); switch(b) {case a: break; case new A(): break; case b: b = a;} assert b == a && b.equals(a);");
	}
}
