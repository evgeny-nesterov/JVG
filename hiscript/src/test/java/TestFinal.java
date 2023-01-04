import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestFinal extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("final int x; x = 1; assert x == 1;");
		assertSuccessSerialize("final String x; x = \"a\"; assert x.equals(\"a\");");
		assertSuccessSerialize("final String x; x = null; assert x == null;");

		assertFailCompile("final int x = 1; x = 1;");
		assertFailCompile("final String x = 1; x = null;");
		assertFailCompile("final var x = 1; x = 2;");

		assertFailCompile("class A{final int x; {x = 1;}}");
		assertFailCompile("class A{final int x = 1; {x = 1;}}");
		assertFailCompile("class A{final static int x = 1; static{x = 1;}}");
		assertFailCompile("class A{void a(final int a){a = 1;}}");
		assertFailCompile("class A{A(final int a){a = 1;}}");
		assertFailCompile("try{int x = 0;}catch(final Exception e){e = null;}");
		assertFailCompile("class A{void a(final int... a){a = null;}}");

		assertFailCompile("interface I{int x = 1;} I.x = 2;");
		assertFailCompile("interface I{static int x = 1;} I.x = 2;");
	}
}