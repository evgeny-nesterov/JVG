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
		execute("String[] arr = new String[]{\"a\", \"b\", \"c\"}; int i = 0; for(String s : arr){System.println(\"arr[\" + i++ + \"]=\" + s);}");
	}
}
