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
		// assertFailCompile("class A{void m(B arg){}} new A().m(1);");
		// assertSuccess("interface I{int x = 1;} assert I.x == 1;");
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
		// new
		assertFailCompile("int x = 0; x.new Object();");
		assertFailCompile("int[] x = {0}; x.new Object();");
		assertFailCompile("class A{class B{}} A[] a = {new A()}; a.new B();");

		// classes
		assertFailCompile("int x = 0; Object o = x.field;");
		assertFailCompile("int x = 0; x.get();");
		assertFailCompile("class I extends int{}");
		assertFailCompile("class I implements int{}");
		assertFailCompile("class int {}");
		assertFailCompile("class A{} class B implements A{}");
		assertFailCompile("class A{} class B implements A{}");
		assertFailCompile("interface A{} interface B implements A{}");

		// operations
		assertSuccess("Integer x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("var x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("Integer x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("var x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");

		assertSuccess("Boolean x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("var x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("Boolean x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("var x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");

		assertFailCompile("String x = \"\"; int y = x & 1;");
		assertFailCompile("String x = \"\"; int y = x | 1;");
		assertFailCompile("String x = \"\"; int y = x ^ 1;");
		assertFailCompile("Boolean x = true; int y = x & 1;");
		assertFailCompile("Boolean x = true; int y = x | 1;");
		assertFailCompile("Boolean x = true; int y = x ^ 1;");

		for (String o : new String[] {"+", "-", "*", "/", "%", ">", ">=", "<", "<=", "==", "!=", "&", "&&", "|", "||", "^"}) {
			assertFailCompile("boolean x = \"\" " + o + " 1;");
			assertFailCompile("boolean x = \"\" " + o + " true;");
			assertFailCompile("boolean x = 1 " + o + " true;");
		}
		assertSuccess("Boolean x = (boolean) true; assert x;");

		assertSuccess("boolean x = true; x ^= true;");
		assertSuccess("int x = 1; x ^= 1;");
		assertFailCompile("boolean x = true; x ^= 1;");
		assertFailCompile("boolean x = true; x ^= 1.1;");
		assertFailCompile("int x = 1; x ^= true;");
		assertFailCompile("int x = 1; x ^= true;");

		assertFailCompile("int x = 1; x += true;");
		assertFailCompile("int x = 1; x += \"\";");
		assertFailCompile("int x = 1; x -= true;");
		assertFailCompile("int x = 1; x -= \"\";");

		assertSuccess("int x = 2; x >>=1; assert x == 1;");
		assertFailCompile("int x = 2; x >>=true;");
		assertSuccess("int x = 1; x <<=1; assert x == 2;");
		assertFailCompile("int x = 2; x <<=true;");
		assertFailCompile("int x = 1; x <<<=1;", "invalid expression");
		assertSuccess("int x = 2; x >>>=1; assert x == 1;");
		assertFailCompile("int x = 2; x >>>=true;");
		assertFailCompile("int x = 1 >> true;");
		assertFailCompile("int x = 1 << true;");
		assertFailCompile("int x = 1 >>> true;");

		// divide
		assertFailCompile("double x = 1; x *= true;");
		assertFailCompile("double x = 1; x /= true;");
		assertFailCompile("double x = 1; x %= true;");
		assertFailCompile("boolean x = true; x =* 1;");
		assertFailCompile("boolean x = true; x /= 1;");
		assertFailCompile("boolean x = true; x %= 1;");

		// equals
		assertSuccess("var x = true; var y = true; assert x == y;");
		assertSuccess("var x = true; var y = false; assert x != y;");

		// null
		assertSuccess("Object x = null; assert null == x;");
		assertSuccess("Object x = null; assert x == null;");
		assertSuccess("assert null == null;");
		assertSuccess("Object x = null; assert x != \"\";");
		assertSuccess("Object x = null; assert \"\" != x;");
		assertSuccess("assert null != \"\";");
		assertSuccess("assert \"\" != null;");

		// zero
		assertFail("double x = 1; x /= (byte)0;");
		assertFail("double x = 1; x /= (short)0;");
		assertFail("double x = 1; x /= (char)0;");
		assertFail("double x = 1; x /= 0;");
		assertFail("double x = 1; x /= 0L;");
		assertFail("double x = 1; x /= 0f;");
		assertFail("double x = 1; x /= 0.0;");

		assertFail("double x = 1 / (byte)0;", "divide by zero");
		assertFail("double x = 1 / (short)0;", "divide by zero");
		assertFail("double x = 1 / (char)0;", "divide by zero");
		assertFail("double x = 1 / 0;", "divide by zero");
		assertFail("double x = 1 / 0L;", "divide by zero");
		assertFail("double x = 1 / 0f;", "divide by zero");
		assertFail("double x = 1 / 0.0;", "divide by zero");

		assertFail("double x = 1; byte y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; short y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; char y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; int y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; long y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; float y = 0; double z = x / y;", "divide by zero");
		assertFail("double x = 1; double y = 0; double z = x / y;", "divide by zero");

		// var
		assertFailCompile("!true;");
		assertFailCompile("1++;");
		assertFailCompile("1--;");
		assertFailCompile("++1;");
		assertFailCompile("--1;");

		// array
		assertFail("int[] x = {1}; int y = x[-1];");
	}
}