import org.junit.jupiter.api.Test;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertSuccessSerialize("class A{static int xxx = 1;} class B extends A{} assert new B().xxx == 1;");
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
		//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
		//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// strings
		assertSuccessSerialize("assert \"123\".toString().equals(\"123\");");
		assertFail("String s = null; s.toString();", //
				"null pointer");
	}
}