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
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertFailCompile("int x = 1; int y = switch(x){case 1 -> 10; case 2 -> true;};", //
				"incompatible switch values types; found boolean, required int");
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