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
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// assertFailCompile("switch(\"\"){case String s: break;}"); // default required
		// assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
		// assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases

		// assertSuccess("class C{static int x = 1; static C get(){return null;}} assert C.get().x == 1;"); // compiler change C.get() to C
		// assertSuccess("float f1 = 0.0f; float f2 = -0.0f; assert f1 != f2;");
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// float
		assertSuccess("assert 0.0f == -0.0f;");

		// methods overriding
		assertSuccess("class A{int m(Object o){return 1;} int m(Number o){return 2;} int m(Integer o){return 3;}} assert new A().m(null) == 3;");
		assertSuccess("class A{ int m(Integer o){return 3;} int m(Object o){return 1;} int m(Number o){return 2;}} assert new A().m(null) == 3;");

		// enums
		assertSuccess("enum E{a,b}; E[] values = E.values(); assert values.length == 2; assert values[0] == E.a; assert values[1] == E.b;");
		assertSuccess("enum E{a,b}; assert E.valueOf(\"a\") == E.a;  assert E.valueOf(\"b\") == E.b;  assert E.valueOf(\"c\") == null;");

		// strings
		assertSuccess("class A{static String a = \"abc\";} class B{static String b = \"abc\";} assert A.a == B.b;"); // string pool

		// numbers pools
		assertSuccess("Byte a = 127; Byte b = 127; assert a == b;");
		assertSuccess("Byte a = -128; Byte b = -128; assert a == b;");
		assertSuccess("Integer a = 127; Integer b = 127; assert a == b;");
		assertSuccess("Integer a = -128; Integer b = -128; assert a == b;");
		assertSuccess("Integer a = 128; Integer b = 128; assert a != b;");
		assertSuccess("Integer a = -129; Integer b = -129; assert a != b;");
	}
}