import org.junit.jupiter.api.Test;

public class TestFinal extends HiTest {
	@Test
	public void testField() {
		assertSuccessSerialize("final int x; x = 1; assert x == 1;");
		assertSuccessSerialize("final String x; x = \"a\"; assert x.equals(\"a\");");
		assertSuccessSerialize("final String x; x = null; assert x == null;");
		assertSuccessSerialize("final int x = 1; class A{int y; A(){this.y = x;}} new A();");
		assertSuccessSerialize("final int x = 1; class A{int y = x;} new A();");

		assertFailCompile("final int x = 1; x = 1;");
		assertFailCompile("final int x; x = 1; x = 2;");
		assertFailCompile("final String x = \"\"; x = null;");
		assertFailCompile("final var x = 1; x = 2;");

		assertFailCompile("class A{final int x; {x = 1;}}");
		assertFailCompile("class A{final int x = 1; {x = 1;}}");
		assertFailCompile("class A{final static int x = 1; static{x = 1;}}");
		assertFailCompile("class A{void a(final int a){a = 1;}}");
		assertFailCompile("class A{A(final int a){a = 1;}}");
		assertFailCompile("try{int x = 0;}catch(final Exception e){e = null;}");
		assertFailCompile("class A{void a(final int... a){a = null;}}");
		assertFailCompile("final int x = 1; class A{{x = 1;}}");

		// number operations which changes value
		assertFailCompile("final int x; x += 1;");
		assertFailCompile("final int x = 1; x += 1;");
		assertFailCompile("final int x = 1; x -= 1;");
		assertFailCompile("final int x = 1; x |= 1;");
		assertFailCompile("final int x = 1; x &= 1;");
		assertFailCompile("final int x = 1; x ^= 1;");
		assertFailCompile("final int x = 1; x ~= 1;");
		assertFailCompile("final int x = 1; x >>= 1;");
		assertFailCompile("final int x = 1; x <<= 1;");
		assertFailCompile("final int x = 1; x >>>= 1;");
		assertFailCompile("final int x = 1; x <<<= 1;");
		assertFailCompile("final int x = 1; x %= 1;");
		assertFailCompile("final int x = 1; x++;");
		assertFailCompile("final int x = 1; ++x;");
		assertFailCompile("final int x = 1; x--;");
		assertFailCompile("final int x = 1; --x;");
		assertFailCompile("for(final int i = 0; i < 10; i++){}");

		// boolean operations which changes value
		assertFailCompile("final boolean x = true; x &= true;");
		assertFailCompile("final boolean x = true; x &&= true;");
		assertFailCompile("final boolean x = true; x |= true;");
		assertFailCompile("final boolean x = true; x ||= true;");

		// interface field is final on default
		assertFailCompile("interface I{int x = 1;} I.x = 2;");
		assertFailCompile("interface I{static int x = 1;} I.x = 2;");
	}
}