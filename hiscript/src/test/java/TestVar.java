import org.junit.jupiter.api.Test;

public class TestVar extends HiTest {
	@Test
	public void test() {
		assertSuccess("int x = 1; assert x == 1;}");
		assertSuccess("var x1 = 123; var x2 = x1; var x3 = x2; var x4 = x3; var x5 = x4; var x6 = x5; assert x5 == x1; assert x6 == 123;}");
		assertSuccess("var x = new Object(); assert x instanceof Object; x.hashCode(); var y = x; assert x.equals(y);");
		assertSuccess("var x = 1; int y = x + 1; assert x == 1; assert y == 2;");
		assertSuccess("var x = false; x = !x; assert x;");
		assertSuccess("var x = \"abc\"; assert x instanceof String; assert x.equals(\"abc\");");
		assertSuccess("class A{} var x = new A(); assert x instanceof A;");
		assertSuccess("var x = new int[]{1, 2}; assert x instanceof int[]; assert x.length == 2; assert x[1] == 2;");
		assertSuccess("var x = 1; var y = x; assert y == 1;");
		assertSuccess("var x = \"abc\"; var y = x; assert y.equals(\"abc\"); assert y == x;");
		assertSuccess("var x = new String(\"abc\"); var y = x; assert x == y; assert y.getClass() == String.class; assert y.length() == 3;");

		assertSuccess("class A{var x = 1; {assert x == 1;} {x = 2;}} A a = new A(); assert a.x == 2;");
		assertSuccess("class A{int get(int x) {var y = x + 1; return y;}} A a = new A(); assert a.get(1) == 2;");

		assertFailCompile("class A{var x; {int x = 1}}", //
				"var is not initialized");
		assertFailCompile("class A{static var x; static{int x = 1}}", //
				"var is not initialized");
		assertFailCompile("class A{void get(var x) {}}", //
				"'var' not allowed here");
		assertFailCompile("class A{var get() {}}", //
				"'var' not allowed here");

		assertSuccess("var x = 1; class C{var y = x; {assert y == 1;} long getY(){return y;}} var z = new C().getY(); assert z == 1; }");
		assertSuccess("var x = 1; record R(int x); var r = new R(x); assert r.getX() == 1;");

		// nulls
		assertSuccess("var x = (String)null;");
		assertFailCompile("var x = null;", //
				"invalid var initialization");

		// fails
		assertFailCompile("var x;", //
				"var is not initialized");
		assertFailCompile("var x; x = 1;", //
				"var is not initialized");
		assertFailCompile("int x = 1; assert x instanceof Integer;}", // x is primitive
				"inconvertible types; cannot cast int to Integer");
	}
}