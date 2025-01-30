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
//		=======================
		assertFailCompile("int x = 0; x.new Object();");
		assertFailCompile("int[] x = {0}; x.new Object();");
		assertFailCompile("class A{class B{}} A[] a = {new A()}; a.new B();");
		assertFailCompile("int x = 0; Object o = x.field;");
		assertFailCompile("int x = 0; x.get();");
		assertFailCompile("class I extends int{}");
		assertFailCompile("class I implements int{}");
		assertFailCompile("class int {}");
		assertFailCompile("class A{} class B implements A{}");
		assertFailCompile("class A{} class B implements A{}");
		assertFailCompile("interface A{} interface B implements A{}");
//		=======================
//		=======================

//		TODO assertFailCompile("switch(\"\"){case Integer i, String s: break;}"); // several types in one case
//		TODO assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		TODO assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		TODO assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases

//		assertSuccess("Integer x = 2; Integer y = 3; assert (x & y) == 2;");
	}
}