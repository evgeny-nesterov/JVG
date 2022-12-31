import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestFinal extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("final int x; x = 1;");
		assertSuccessSerialize("final String x; x = \"a\";");
		assertSuccessSerialize("final String x; x = null;");

		assertFailSerialize("final int x = 1; x = 1;");
		assertFailSerialize("final String x = 1; x = null;");
		assertFailSerialize("final var x = 1; x = 2;");

		assertFailSerialize("class A{final int x = 1; {x = 1;}}");
		assertFailSerialize("class A{final static int x = 1; static{x = 1;}}");
		assertFailSerialize("class A{void a(final int a){a = 1;}}");
		assertFailSerialize("class A{A(final int a){a = 1;}}");
		assertFailSerialize("try{int x = 0;}catch(final Exception e){e = null;}");
		assertFailSerialize("class A{void a(final int... a){a = null;}}");

		assertFailCompile("interface I{int x = 1;} I.x = 2;");
		assertFailCompile("interface I{static int x = 1;} I.x = 2;");
	}
}