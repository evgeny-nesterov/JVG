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
	}

	@Test
	public void testTodo() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
//		TODO assertFailCompile("switch(\"\"){case Integer i, String s: break;}"); // several types in one case
//		TODO assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		TODO assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		TODO assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
	}
}