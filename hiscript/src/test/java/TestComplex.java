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
		// assertFailCompile("++1;", // TODO variable expected, operations = [null] ???
		//		"not a statement");

		// Tasks
		// 1. assertSuccess("class C{static int x = 1; static C get(){return null;}} assert C.get().x == 1;"); // compiler change C.get() to C
		// 2. assertSuccess("float f1 = 0.0f; float f2 = -0.0f; assert f1 != f2;");
		// 3. assertSuccess("Float f1 = new Float(Float.NaN); Float f2 = new Float(Float.NaN); assert f1 != f2; assert f1.equals(f2); assert Float.NaN != Float.NaN;");
		// 4. assertSuccess("class A{static String ab = \"ab\"; static String a = \"a\"; static String b = \"b\";} class B{static String ab = \"ab\";} assert A.ab == B.ab; assert A.ab == (A.a + A.b); String b = \"b\"; assert A.ab != A.a + b; assert A.ab == (A.a + b).intern(); final String fb = \"b\"; assert A.ab == (A.a + fb);");
		// 5. assertSuccess("");
		// 6. assertSuccess("");
		// 7. assertSuccess("");
		// 8. assertSuccess("");
		// 9. assertSuccess("");
		// 10. assertSuccess("");
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

		// records
		assertFailCompile("record R1(int a); record R2(int a, int b) extends R1;", //
				"no extends clause allowed for record");
		assertFailCompile("class A{} record R(int a) extends A;", //
				"no extends clause allowed for record");
		assertFailCompile("record R(int a); class A extends R {}", //
				"the type A cannot subclass the final class R");
		assertFailCompile("abstract record R(int a);", //
				"modifier 'abstract' is not allowed");
		assertSuccess("record R(int a, boolean b); assert new R(1, true).toString().equals(\"R[a=1, b=true]\");"); // record toString
		assertSuccess("interface A{int getA();} record R(int a) implements A; A a = new R(1); assert a.getA() == 1;"); // record implements

		// number (min/max values)
		assertSuccess("assert Byte.MAX_VALUE + Byte.MIN_VALUE == -1;");
		assertSuccess("byte x = Byte.MAX_VALUE; x++; assert x == Byte.MIN_VALUE;");
		assertSuccess("byte x = Byte.MIN_VALUE; x--; assert x == Byte.MAX_VALUE;");

		assertSuccess("assert Short.MAX_VALUE + Short.MIN_VALUE == -1;");
		assertSuccess("short x = Short.MAX_VALUE; x++; assert x == Short.MIN_VALUE;");
		assertSuccess("short x = Short.MIN_VALUE; x--; assert x == Short.MAX_VALUE;");

		assertSuccess("assert Integer.MAX_VALUE + Integer.MIN_VALUE == -1;");
		assertSuccess("int x = Integer.MAX_VALUE; x++; assert x == Integer.MIN_VALUE;");
		assertSuccess("int x = Integer.MIN_VALUE; x--; assert x == Integer.MAX_VALUE;");

		assertSuccess("assert Long.MAX_VALUE + Long.MIN_VALUE == -1;");
		assertSuccess("long x = Long.MAX_VALUE; x++; assert x == Long.MIN_VALUE;");
		assertSuccess("long x = Long.MIN_VALUE; x--; assert x == Long.MAX_VALUE;");

		assertSuccess("char x = Character.MAX_VALUE; x++; assert x == Character.MIN_VALUE;");
		assertSuccess("char x = Character.MIN_VALUE; x--; assert x == Character.MAX_VALUE;");

		// strings
		assertSuccess("assert Integer.toString(1) != Integer.toString(1);");
		assertSuccess("assert Integer.toString(1).intern() == Integer.toString(1).intern();");

		// strings compilation
		assertSuccess("String ab = \"ab\";  String b = \"b\"; assert ab != \"a\" + b;");
		assertSuccess("String ab = \"ab\";  assert ab == \"a\" + \"b\";");
		assertSuccess("String abc = \"abc\";  assert abc == \"a\" + \"b\" + \"c\";");
		assertSuccess("String abc = \"abc\";  assert abc == \"a\" + (\"b\" + \"c\");");
		assertSuccess("String abc = \"abc\";  assert abc == (\"a\" + \"b\") + \"c\";");
		assertSuccess("String abcd = \"abcd\";  assert abcd == (\"a\" + ((\"b\") + \"c\")) + \"d\";");
		assertSuccess("String a = \"a\"; String abc1 = \"abc\"; String abc2 = a + \"b\" + \"c\"; assert abc1 != abc2; assert abc1.equals(abc2);");
		assertSuccess("String a = \"a\"; String abc2 = a + \"b\" + \"c\"; String abc1 = \"abc\";  assert abc1 == abc2; assert abc1.equals(abc2);");

		// compile value
		assertSuccess("""
     				int a = 1;
     				int b = 2;
					int c = 1 + 2 * switch(1 + 2 * 1) {
					    case 1 ->                   2;
					    case 2, 3 ->                2 * 2 - 1; // matched
					    case 3, a + 2, 2 + 1 ->     2 + b; // never return
					};
					assert c == 7;
				""");
	}
}