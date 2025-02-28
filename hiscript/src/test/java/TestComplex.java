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
		assertSuccess("boolean x = true && true; assert x;");
		assertSuccess("boolean x = 1 == 1 && 2 == 1 + 1; assert x;");

		// cast
		assertSuccess("boolean x = true; boolean y = (boolean)x; assert x == y;");
		String[] primitives = {"byte", "short", "char", "int", "long", "float", "double"};
		String[] boxes = {"Byte", "Short", "Character", "Integer", "Long", "Float", "Double"};
		for (int i1 = 0; i1 < primitives.length; i1++) {
			String t1 = primitives[i1];
			for (int i2 = 0; i2 < primitives.length; i2++) {
				String t2 = primitives[i2];
				assertSuccess(t1 + " x = (" + t1 + ")1; " + t2 + " y = (" + t2 + ")x; assert x == y;");
			}
			for (int i2 = 0; i2 < primitives.length; i2++) {
				String t2 = boxes[i2];
				if (i1 == i2) {
					assertSuccess(t2 + " x = (" + t1 + ")1; " + t1 + " y = (" + t1 + ")x; assert x == y;");
				} else {
					assertFailCompile(t2 + " x = (" + t1 + ")1; " + t1 + " y = (" + t1 + ")x; assert x == y;", //
							"cannot cast " + t2 + " to " + t1);
				}
			}
			assertFailCompile("boolean x = true; " + t1 + " y = (" + t1 + ")x;", //
					"cannot cast boolean to " + t1);
			assertFailCompile("String x = \"\"; " + t1 + " y = (" + t1 + ")x;", //
					"cannot cast String to " + t1);
			assertFailCompile(t1 + " x = (" + t1 + ")null;", //
					"cannot cast null to " + t1);
			assertFailCompile("Object x = null; " + t1 + " y = (" + t1 + ")x;", //
					"cannot cast Object to " + t1);
			assertFailCompile("int[] x = {1}; " + t1 + " y = (" + t1 + ")x;", //
					"cannot cast int[] to " + t1);
		}

		// equate
		assertFailCompile("int x; long y = 1L; x = y;", //
				"operator '=' can not be applied to int, long");
		assertFailCompile("int x; float y = 1F; x = y;", //
				"operator '=' can not be applied to int, float");
		assertFailCompile("int x; double y = 1D; x = y;", //
				"operator '=' can not be applied to int, double");

		assertFailCompile("long x; float y = 1F; x = y;", //
				"operator '=' can not be applied to long, float");
		assertFailCompile("long x; double y = 1D; x = y;", //
				"operator '=' can not be applied to long, double");

		assertFailCompile("float x; double y = 1D; x = y;", //
				"operator '=' can not be applied to float, double");

		// operations
		assertSuccess("assert \"\" != null;");
		assertSuccess("assert true == true;");
		assertSuccess("long x = ~(byte)1;");
		assertSuccess("long x = ~(short)1;");
		assertSuccess("long x = ~(char)1;");
		assertSuccess("long x = ~1;");
		assertSuccess("long x = ~1L;");
		assertSuccess("float x = 1f; float y = -x;");
		assertSuccess("double x = 1d; double y = -x;");
		assertSuccess("float x = 1f; float y = +x;");
		assertSuccess("double x = 1d; double y = +x;");

		// arrays
		assertFailCompile("int x = 1; int y = x[0];", //
				"operator '[]' can not be applied to int, int");

		// class and field priority
		assertFailCompile("class A{class B{static int C = 2;} int B = 1;} int C = A.B.C;", // field B has more priority than class B in the same level
				"non-static field 'B' cannot be accessed from static context");

		// switch
		assertFail("switch(\"a\"){case \"\" + 1/0: break;}", //
				"divide by zero");
		assertSuccess("record R(Number a); switch(new R(1.1f)){case R(Byte a): break; case R(Float a): assert a == 1.1f; return;} assert false;");
		assertSuccess("switch(\"a\") {case false: break; case true: return;} assert false;");
		assertFail("class A{public boolean equals(Object o){throw new RuntimeException(\"exception in equals\");}} switch(new A()) {case new A(): assert false;} assert false;", "exception in equals");

		// try
		assertSuccess("class A{int get(){try{return 1;} finally{}}} assert new A().get() == 1;");

		// class initialization
		assertSuccess("class A{{assert false;} int x = 1/0;} A a;");
		assertSuccess("class A{{assert false;} int x = 1/0;} class B{void m(A a){int x = a.x;}}; new B();");
		assertSuccess("class A{static int x = 1;} class B extends A{{assert false;} int y = 1/0;} assert B.x == 1;");
		assertSuccess("class A{static int m(){return 1;}} class B extends A{{assert false;} int y = 1/0;} assert B.m() == 1;");
		assertSuccess("class A{A(){assert getX() == 0;}; int getX(){return 1;}} class B extends A{int x = 2; int getX(){return x;}}");

		// rewrite field
		assertSuccess("class A{int x = 1; {assert x == 1;}} class B extends A{int x = 2; {assert x == 2;}} new B();");

		// statements
		assertFailCompile("() -> {};", //
				"not a statement");
		assertFailCompile("int m(){return 0;}", //
				"not a statement");
		assertFailCompile("void m(){}", //
				"unexpected token");
		assertFailCompile("C(){}", //
				"';' is expected", //
				"cannot resolve method 'C'");

		// lambda
		assertFailCompile("interface A{int get(int x);} A a = ()->{int x = 0;};", //
				"incompatible parameters signature in lambda expression");

		// class initialization
		assertFail("class A{static {m(1);} static void m(int x){m(x + 1);}} A a = new A();", //
				"cannot initialize class A: stack overflow");
	}
}