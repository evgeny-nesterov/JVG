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
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert A.a.equals(\"nullBA\"); assert B.b.equals(\"nullB\");");
	}
}
