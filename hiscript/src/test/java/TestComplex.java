import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.compiler.ParserUtil;
import ru.nest.hiscript.ool.model.Compiler;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccess(ParserUtil.readString(Compiler.class.getResourceAsStream("/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws IOException {
		// FAIL: native method '@root$1A_void_m' not found
		// assertSuccessSerialize("class A {void m(){}} new A().m();");
		assertFail("class A{static class B{}} A a = new A(); A.B b = a.new B();");
	}
}