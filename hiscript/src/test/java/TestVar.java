import org.junit.jupiter.api.Test;

public class TestVar extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("var x = new Object(); assert x instanceof Object; x.hashCode(); assert x.equals(x);");
		assertSuccessSerialize("var x = 1; int y = x + 1; assert x == 1; assert y == 2;");
		assertSuccessSerialize("var x = false; x = !x; assert x;");
		assertSuccessSerialize("var x = \"abc\"; assert x instanceof String; assert x.equals(\"abc\");");
		assertSuccessSerialize("class A{} var x = new A(); assert x instanceof A;");
		assertSuccessSerialize("var x = new int[]{1, 2}; assert x instanceof int[]; assert x.length == 2; assert x[1] == 2;");
		assertSuccessSerialize("var x = 1; var y = x; assert y == 1;");
		assertSuccessSerialize("var x = \"abc\"; var y = x; assert y.equals(\"abc\"); assert y == x;");
		assertSuccessSerialize("var x = new String(\"abc\"); var y = x; assert x == y; assert y.getClass() == String.class; assert y.length() == 3;");
		assertFailCompile("var x;");
		assertFailCompile("var x; x = 1;");
		assertFailCompile("var x = null;");

		assertSuccessSerialize("class A{var x = 1; {assert x == 1;} {x = 2;}} A a = new A(); assert a.x == 2;");
		assertFailCompile("class A{var x; {int x = 1}}");
		assertFailCompile("class A{static var x; static{int x = 1}}");
		assertSuccessSerialize("class A{int get(int x) {var y = x + 1; return y;}} A a = new A(); assert a.get(1) == 2;");
		assertFailCompile("class A{int get(var x) {return x;}}");
	}
}