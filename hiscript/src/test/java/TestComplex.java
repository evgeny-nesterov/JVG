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
//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// vararg
		assertFailCompile("class C{C(int.. x){}}", //
				"unexpected token");

		// ??? assertSuccessSerialize("String s = \"'\";");

		// chars
		assertFailCompile("char c1 = 'a;\nchar c2 = 'a';", //
				"' is expected");

		// int numbers
		assertFailCompile("int x = _1;", //
				"cannot resolve symbol '_1'");
		assertFailCompile("int x = 1_;", //
				"illegal underscore");
		assertFailCompile("int x = 1__________;", //
				"illegal underscore");
		// hex numbers
		assertSuccessSerialize("int x = -0xf_f; assert -x == 255;");
		assertFailCompile("int x = 0xFF_;", //
				"illegal underscore");
		assertFailCompile("int x = 0x_FF;", //
				"illegal underscore");
		assertFailCompile("long x = 0xFF__________L;", //
				"illegal underscore");
		// binary numbers
		assertSuccessSerialize("int x = 0b101; assert x == 5;");
		assertSuccessSerialize("long x = 0b101L; assert x == 5L;");
		assertSuccessSerialize("int x = -0b101; assert -x == 5;");
		assertFailCompile("int x = 0b10000000000_0000000000_0000000000_00;", // 32 bits
				"integer number too large");
		assertFailCompile("long x = 0b10000000000_0000000000_0000000000_0000000000_0000000000_0000000000_000L;", // 64 bits
				"long number too large");
		assertFailCompile("inx x = 0b2;", //
				"binary numbers must contain at least one binary digit");
		assertFailCompile("inx x = 0b111_;", //
				"illegal underscore");
		// octal numbers
		assertSuccessSerialize("int x = 010; assert x == 8;");
		assertSuccessSerialize("long x = 010L; assert x == 8L;");
		assertSuccessSerialize("int x = -010; assert -x == 8;");
		assertFailCompile("int x = 010000000000_0;", // 11 octals = 11*3=33 bits > 32 bits (max)
				"integer number too large");
		assertFailCompile("long x = 010000000000_0000000000_00L;", // 22 octals = 22*3=66 bits > 64 bits (max)
				"long number too large");
		assertSuccessSerialize("float x = 077f; assert x == 77;"); // non octal
		assertSuccessSerialize("double x = 077d; assert x == 77;"); // non octal
		assertSuccessSerialize("double x = 077e1; assert x == 770;"); // non octal
		assertFailCompile("int x = 077_;", //
				"illegal underscore");
	}
}