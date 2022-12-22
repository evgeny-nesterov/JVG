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
	public void testSingle() throws Exception {
		assertSuccess("record Rec(int a, String b); assert new Rec(1, \"abc\").equals(new Rec(1, \"abc\"));");
		assertSuccess("record Rec(int a, String b); assert new Rec(1, \"abc\").equals(new Rec(1, \"abc\"));");
	}
}
