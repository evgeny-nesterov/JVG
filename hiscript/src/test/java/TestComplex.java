import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.HiCompiler;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/oolTestFully.hi")));
	}

	@Test
	public void testSingle() {
		// byte a = 128;
		// short a = 123121;
		//int a = 2323323220;
		// byte a = 1000 / 3 / 2;
		// assertSuccess("byte a = 64*2 - 101 + 200/2;");
		//assertFail("byte a = 1000 / 3 / 2;");
	}
}
