import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.HiScript;

public class TestExpression extends HiTest {
	@Test
	public void testSimpleNumberExpressions() {
		assertSuccessSerialize("int x = 1 + 100_000_000;");

		assertSuccessSerialize("int a = 1 + 2; assert a == 3;");
		assertSuccessSerialize("int a = 10 - 1; assert a == 9;");
		assertSuccessSerialize("int a = 5 * 6; assert a == 30;");
		assertSuccessSerialize("int a = 121 / 11; assert a == 11;");
		assertSuccessSerialize("int a = 10 % 9; assert a == 1;");

		assertCondition("long a = 1l + 2;", "a == 3", "long plus");
		assertCondition("long a = 10 - 1;", "a == 9", "long minus");
		assertCondition("long a = 5L * 6L;", "a == 30", "long multiply");
		assertCondition("long a = 121 / 11;", "a == 11", "long divide");
		assertCondition("long a = 10 % 9l;", "a == 1", "long %");

		assertCondition("double a = 1d + 2.0;", "a == 3.0", "double plus");
		assertCondition("double a = 10L - 1.0;", "a == 9.0", "double minus");
		assertCondition("double a = 5D * 6.0D;", "a == 30.0", "double multiply");
		assertCondition("double a = 121f / 11.0;", "a == 11.0", "double divide");
		assertCondition("double a = 10 % 9.0;", "a == 1.0", "double %");

		assertCondition("float a = 1 + 2.0f;", "a == 3.0", "float plus");
		assertCondition("float a = 10 - 1F;", "a == 9.0", "float minus");
		assertCondition("float a = 5 * 6;", "a == 30.0", "float multiply");
		assertCondition("float a = 121f / 11;", "a == 11.0", "float divide");
		assertCondition("float a = 10f % 9.0f;", "a == 1.0", "float %");

		assertSuccess("byte a = " + Byte.MAX_VALUE + "; assert a == " + Byte.MAX_VALUE + ";");
		assertSuccess("byte a = " + Byte.MIN_VALUE + "; assert a == " + Byte.MIN_VALUE + ";");
		assertSuccess("short a = " + Short.MAX_VALUE + "; assert a == " + Short.MAX_VALUE + ";");
		assertSuccess("short a = " + Short.MIN_VALUE + "; assert a == " + Short.MIN_VALUE + ";");

		assertSuccessSerialize("int a = 2; a /= 2; assert a == 1;");
		assertSuccess("int a = 1; a *= 2; assert a == 2;");
		assertSuccess("int a = 1; a += 1; assert a == 2;");
		assertSuccess("int a = 1; a -= 1; assert a == 0;");
		assertSuccess("int a = 1; a |= 2; assert a == 3;");
		assertSuccess("int a = 1 | 2; assert a == 3;");
		assertSuccess("int a = 3; a &= 2; assert a == 2;");
		assertSuccess("int a = 3 & 2; assert a == 2;");
		//		assertSuccess("int a = 11; a >>>= 2; assert a == " + (11 >>> 2) + ";");
		assertSuccess("int a = 11; a >>= 2; assert a == " + (11 >> 2) + ";");
		assertSuccess("int a = 11; a <<= 2; assert a == " + (11 << 2) + ";");
		assertSuccess("int a = 11>>2; assert a == " + (11 >> 2) + ";");
		assertSuccess("int a = 11<<2; assert a == " + (11 << 2) + ";");
		assertSuccess("int a = 11; a ^= 7; assert a == " + (11 ^ 7) + ";");
		assertSuccess("int a = 11 ^ 7; assert a == " + (11 ^ 7) + ";");
		assertSuccess("boolean a = true; a &= false; assert !a;");
		assertSuccess("boolean a = false; a |= true; assert a;");
	}

	@Test
	public void testComplexNumberExpressions() {
		assertCondition("int a = (10 + 5) & 7; System.println(\"a=\" + a);", "a == 7", "expression 2");
		assertCondition("int a = (2 - +1) * +-+ + -2 / - + - + +2 /*=1*/    +    (((1))) * (2 -1/(int)1 + 1*0) /*=1*/    -    16/2/2/2 /*=2*/; System.println(\"a=\" + a);", "a == 0", "expression 1");
		assertSuccessSerialize("assert (1 + (int)1.1 + 1) == 3;");
		assertSuccessSerialize("assert (1 + (int)2.1 / (byte)2 + 1) == 3;");
		assertSuccessSerialize("assert ((int)1.9f + (int)1 - (float)2) == 0.0;");
		assertSuccessSerialize("assert ((int)1.9f + (float)(int)3 / (float)2) == 2.5;");
		assertSuccessSerialize("int x = 5; assert (- -+ -x + - (int) + (float) - + + + + +(double)x) == 0;");
		assertSuccessSerialize("assert 2 * 6 / 3 + 4 * 5 / (20 - 9 * 2) / 2 == 9;");
		assertSuccessSerialize("assert (1 >= 0 ? 1 + 2 : 2 + 3) == 3;");
		assertSuccessSerialize("assert (1 > 2 ? 3 > 4 ? 5 : 6 : 7 > 8 ? 9 : 10) == 10;");
		assertSuccessSerialize("assert 16/-2/2/-2/+2*2/2 == 1;");
		assertSuccessSerialize("assert 2/+ -2 == -100000000000L/100000000000L;");
		assertSuccessSerialize("assert true;");
	}

	@Test
	public void testNumbersCasts() {
		String[] prefixes1 = {" ", "(byte)", "(short)", "(int)", "(long)", "(double)", "(float)", "(char)"};
		String[] prefixes2 = {"+", "- -", "+ +", " "};
		String[] suffixes = {"", ".", ".0", ".0000", "f", "F", "d", "D", "l", "L", ".d", ".D", ".0d", ".0D", "e+0", "e-0", "0e-1"};
		testSuccessNumbersCasts(prefixes1, prefixes2, suffixes, new String[] {"1", "127"});

		assertSuccessSerialize("assert (byte)" + (Byte.MAX_VALUE + 1) + " == " + (byte) (Byte.MAX_VALUE + 1) + ";");
		assertSuccessSerialize("assert (byte)" + (Byte.MIN_VALUE - 1) + " == " + (byte) (Byte.MIN_VALUE - 1) + ";");
		assertSuccessSerialize("assert (short)" + (Short.MAX_VALUE + 1) + " == " + (short) (Short.MAX_VALUE + 1) + ";");
		assertSuccessSerialize("assert (short)" + (Short.MIN_VALUE - 1) + " == " + (short) (Short.MIN_VALUE - 1) + ";");
		assertSuccessSerialize("assert (int)" + (Integer.MAX_VALUE + 1l) + "L == " + (int) (Integer.MAX_VALUE + 1l) + "L;");
		assertSuccessSerialize("assert ( int )" + (Integer.MIN_VALUE - 1l) + "L == " + (int) (Integer.MIN_VALUE - 1l) + ";");
	}

	private void testSuccessNumbersCasts(String[] prefixes, String[] prefixes2, String[] suffixes, String[] successNumbers) {
		for (String successNumber : successNumbers) {
			for (String prefix1 : prefixes2) {
				for (String prefix2 : prefixes) {
					for (String prefix3 : prefixes2) {
						for (String suffix1 : suffixes) {
							for (String suffix2 : suffixes) {
								String script = "assert " + prefix1 + prefix2 + prefix3 + successNumber + suffix1 + " == " + successNumber + suffix2 + ";";
								assertSuccess(script);
							}
						}
					}
				}
			}
		}
	}

	@Test
	public void testInstanceOf() {
		assertSuccessSerialize("assert \"\" instanceof String;");
		assertSuccessSerialize("assert new Object() instanceof Object;");
		assertSuccessSerialize("assert !(null instanceof String);");
		assertSuccessSerialize("assert !(null instanceof Object);");
		assertSuccessSerialize("String s = \"\"; assert s instanceof Object;");
		assertSuccessSerialize("String s = null; assert !(s instanceof String);");
		assertSuccessSerialize("String s = null; assert !(s instanceof Object);");
		assertSuccessSerialize("Object s = new Object(); assert s instanceof Object;");
		assertSuccessSerialize("Object s = \"\"; assert s instanceof Object;");
		assertSuccessSerialize("Object s = \"\"; assert s instanceof String;");
		assertSuccessSerialize("Object s = null; assert !(s instanceof Object);");
		assertSuccessSerialize("class O1{}; class O2 extends O1{}; class O3 extends O2{}; class O4 extends O3{};" + //
				"assert new O2() instanceof O2;" + //
				"assert new O2() instanceof O1;" + //
				"assert !(new O1() instanceof O2);" + //
				"assert new O4() instanceof O1;");

		assertSuccessSerialize("class A {int a = 1;} class B extends A{int b = 2;}" + //
				"assert new A() instanceof A x? x.a == 1 : false;" + //
				"assert new B() instanceof A y? y.a == 1 : false;" + //
				"assert new B() instanceof B z? z.b == 2 : false;" + //
				"A a = new A();" + //
				"Object a1 = a;" + //
				"a.a = 2;" + //
				"assert ((A)a1).a == 2;" + //
				"if (a1 instanceof A b) {" + //
				"	assert b == a;" + //
				"	assert b == a1;" + //
				"	assert b instanceof A;" + //
				"	assert a.a == 2;" + //
				"	assert b.a == 2;" + //
				"	((A)a1).a = 3;" + //
				"	assert b.a == 3;" + //
				"	assert a.a == 3;" + //
				"} else {" + //
				"	assert false;" + //
				"}");
	}

	@Test
	public void testChars() {
		assertSuccessSerialize("char c = 'a'; assert c == 'a';");
		assertSuccessSerialize("char c = 'a' + 1; assert c == 'b';");
		assertSuccessSerialize("char c = 'b' - 1; assert c == 'a';");
		assertSuccessSerialize("char c = 'a' + (byte)1; assert c == 'b';");
		assertSuccessSerialize("char c = 'a' + (short)1; assert c == 'b';");
		assertSuccessSerialize("char c = 'a' + (int)1; assert c == 'b';");

		assertSuccessSerialize("char c = 1; assert c == 1;");
		assertSuccessSerialize("char c = (byte)1; assert c == 1;");
		assertSuccessSerialize("char c = (short)1; assert c == 1;");
		assertSuccessSerialize("char c = (int)1; assert c == 1;");
		assertSuccessSerialize("char c = (int)1; assert c == 1;");
		assertSuccessSerialize("char c = (char)-1; assert c == 65535;");
		assertSuccessSerialize("int c = (char)-1; assert c == 65535;");

		assertFail("char c1 = -1;");
		assertFail("char c1 = 'a'; char c2 = c1 + '1';");
		assertFail("char c1 = 'a'; char c2 = c1 + 1;");
		assertFail("char c1 = 'a'; char c2 = c1 + 1l;");
		assertFail("char c1 = 'a'; char c2 = c1 + (byte)1;");
		assertFail("char c1 = 'a'; char c2 = c1 + (short)1;");

		assertSuccessSerialize("char c = \"x\".charAt(0); assert c == 'x';");
		assertSuccessSerialize("String s = \"[\" + 'a' + 'b' + 'c' + \"]\"; assert s.equals(\"[abc]\");");

		assertSuccessSerialize("int c = 'a'; c++; assert c == 'b';");
		assertSuccessSerialize("int c = 'a'; ++c; assert c == 'b';");
		assertSuccessSerialize("int c = 'b'; c--; assert c == 'a';");
		assertSuccessSerialize("int c = 'b'; --c; assert c == 'a';");

		assertSuccessSerialize("String s = \"\"; for (char c = '1'; c <='9'; c++) {s += c;}; System.println(\"s=\" + s); assert s.equals(\"123456789\");");
	}

	@Test
	public void testLogicalSwitch() {
		assertSuccessSerialize("assert 1>0 ? true : false;");
		assertSuccessSerialize("assert 1>0 ? true : false;");
		assertSuccessSerialize("assert 1+1*2/1>2-2 ? 1/1>0*2 : 0*2>1/1;");
		assertSuccessSerialize("assert 1>0?1>0?true:false:false;");
		assertSuccessSerialize("assert 1>0?1<0?false:true:false;");
		assertSuccessSerialize("assert 1<0?1<0?false:false:true;");
		assertSuccessSerialize("assert 1>0?1>0?1>0?true:false:false:false;");
		assertSuccessSerialize("assert 1>0?1<0?1>0?false:false:true:false;");
		assertSuccessSerialize("assert 1>0?1>0?1<0?false:true:false:false;");
		assertSuccessSerialize("assert 1<0?1>0?1>0?false:false:false:true;");
		assertSuccessSerialize("assert 1>0?1>0?1>0?1>0?true:false:false:false:false;");
	}

	@Test
	public void testLoad() throws Exception {
		HiScript script = HiScript.create().compile("int x = 0;").execute().printError().compile("x = x + 1;");
		for (int i = 0; i < 1_000_000; i++) {
			script.execute().printError();
		}
		script.compile("System.println(\"x=\" + x); assert x == 1_000_000;").execute().printError().close();
		System.out.println("load test duration: " + script.duration() / 1000.0 + "sec");
	}
}
