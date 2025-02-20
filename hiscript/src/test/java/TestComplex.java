import org.junit.jupiter.api.Test;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccess(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertSuccess("record A(int x); record B(A a); record C(B b); record D(C c); " + //
				"Object o = new D(new C(new B(new A(123))));" + //
				"if(o instanceof D(C(B(A(int X) a) b) c) d){assert X == 123;}");
		assertSuccess("record A(int x); record B(A a); record C(B b); record D(C c); " + //
				"Object o = new D(new C(new B(new A(123))));" + //
				"switch(o) {case D(C(B(A(int X) a) b) c) d: assert X == 123; return;} assert false;");
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
		//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
		//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
	}
}