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
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		//execute("interface A{int getLength();} A a = \"abc\"::length; assert a.getLength() == 3;");
		//execute("interface I{String get(long x, String y);} I o = (x, y) -> x + y; assert o.get((byte)1, \"_\").equals(\"1_\");");
		execute("class C{int c = 0;} C c = new C(); interface I{void m(I i, int x);} I o = (i,x)->{if(x>0) i.m(i, x-1); c.c++;}; o.m(o, 5); assert c.c == 6;");
	}
}