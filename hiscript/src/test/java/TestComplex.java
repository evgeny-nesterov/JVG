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
		assertSuccessSerialize("int x;\n\nint y;\n\n");
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// vararg
		assertFailCompile("class C{C(int.. x){}}", //
				"unexpected token");

		// string
		assertSuccessSerialize("String s = \"'\";");

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
		assertFailCompile("int x = 0b2;", //
				"binary numbers must contain at least one binary digit");
		assertFailCompile("int x = 0b111_;", //
				"illegal underscore");
		assertFailCompile("int x = 0b_111;", //
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
		// numbers
		assertFailCompile("float x = 10e200f;", //
				"float number too large");
		assertFailCompile("double x = 10e1000;", //
				"double number too large");

		// arrays
		assertFailCompile("int[] a = new int[1L];", //
				"int is expected");
		assertFailCompile("int[] a = new int[1F];", //
				"int is expected");
		assertFailCompile("int[] a = new int[1D];", //
				"int is expected");
		assertFailCompile("int d = 1L; int[] a = new int[d];", //
				"long cannot be converted to int");
		assertFail("int[] a = new int[-1];", //
				"negative array size");
		assertFail("int[][] a = new int[0][-1];", //
				"negative array size");
		assertFail("int d = -1; int[] a = new int[d];", //
				"negative array size");
		// cast arrays
		assertFail("Object a = new int[0]; double[] b = (double[])a;", //
				"cannot cast int[] to double[]");
		assertFail("Object a = 1; double[] b = (double[])a;", //
				"cannot cast int to double[]");
		assertFail("Object a = new int[0]; Double b = (Double)a;", //
				"cannot cast int[] to Double");

		// priority primitive cast over primitive cast
		String[] types = {"byte", "short", "char", "int", "long", "float", "double"};
		for (int i1 = 0; i1 < types.length; i1++) {
			String a1 = types[i1];
			for (int i2 = 0; i2 < types.length; i2++) {
				if (i1 != i2) {
					String a2 = types[i2];
					for (int i = 0; i < types.length; i++) {
						String a = types[i];
						boolean cm1 = i == i1 || !a1.equals("char");
						boolean cm2 = i == i2 || !a2.equals("char");
						boolean m1 = i <= i1 && (i1 < i2 || i2 < i || !cm2) && (cm1 || !cm2);
						boolean m2 = i <= i2 && (i2 < i1 || i1 < i || !cm1) && (cm2 || !cm1);
						if (m1 || m2) {
							String ms1 = m1 ? "true" : "false";
							String ms2 = m2 ? "true" : "false";
							assertSuccessSerialize("class A {void m(" + a1 + " x){assert " + ms1 + ";} void m(" + a2 + " x){assert " + ms2 + ";}} new A().m((" + a + ")1);");
						} else {
							assertFailCompile("class A {void m(" + a1 + " x){} void m(" + a2 + " x){}} new A().m((" + a + ")1);", //
									"cannot resolve method 'm' in 'A'");
						}
					}
				}
			}
		}

		// expression
		assertFailCompile("int x = *+-;", //
				"expression expected");
		assertFailCompile("int x = **1;", //
				"expression expected");
		assertFailCompile("int x = *%;", //
				"expression expected");

		// tokenizer
		assertFailCompile("ljoi(*&^%^i}_", //
				"unexpected token");

		// annotations
		assertFailCompile("@A class B{}", //
				"cannot resolve class 'A'");

		// switches
		assertSuccessSerialize("switch(new Integer(1)){case 1:return;} assert false;");
		assertFail("class A{boolean m(){throw new RuntimeException(\"error\");}} switch(new A()){case A a when a.m(): assert false;} assert false;", //
				"error");
		assertFailCompile("enum E{a,b} switch(E.a){case 1: assert false;} assert false;", //
				"an enum switch case label must be the unqualified name of an enumeration constant");
		assertSuccessSerialize("enum E{a,b} switch(E.a){case b: break; default: return;} assert false;");

		// methods
		assertFailCompile("interface A{void m(int x); void m(String x);} A a = (x) -> {};", //
				"multiple non-overriding abstract methods found in interface A");
		assertFailCompile("interface A{void m1(int x); void m2(String x);} A a = (x) -> {};", //
				"multiple non-overriding abstract methods found in interface A");
		assertFailCompile("interface A{} A a = (x) -> {};", //
				"no abstract methods found in interface A");

		// lambda + var (x)
		assertSuccessSerialize("interface A{void m(int x);} class B{A a = (x) -> {};}");

		// methods
		assertSuccessSerialize("class A{synchronized void m(){int x = 0;}} new A().m();");

		// interfaces
		assertSuccessSerialize("interface A{int x = 1;} class B implements A{} assert new B().x == 1;");
		assertSuccessSerialize("interface A{int x = 1;} class B implements A{} assert B.x == 1;");

		// statements
		assertSuccessSerialize("class A{Number x;} A a = new A(); a.x = 1.0;");

		// generics
		assertSuccessSerialize("class A<O1, O2 extends O1, O3 extends O2>{O1 o1; O2 o2; O3 o3;} A<Number, Integer, Integer> a = new A<>(); a.o1 = 1.0; a.o2 = 2; a.o3 = new Integer(3);");

		// try-catch
		assertFail("class A implements AutoCloseable{A(){throw new RuntimeException(\"exception in resource initialization\");} public void close(){}} try(A a = new A()){}", //
				"exception in resource initialization");
		assertFail("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"exception in resource close\");}} try(A a = new A()){}", //
				"exception in resource close");
		assertSuccessSerialize("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"exception in resource close\");}} try(A a = new A()){} catch(Exception e){assert e.getMessage().equals(\"exception in resource close\");}");

		// switches
		assertSuccessSerialize("switch(1){case Boolean.TRUE: return;} assert false;");
		assertSuccessSerialize("Object o = new Integer[0]; switch(o){case String[] s: assert false; case Integer[] i: return;} assert false;");
		assertSuccessSerialize("Object o = new Double[0]; switch(o){case Integer[] s: assert false; case Number[] i: return;} assert false;");
		assertFail("record R(boolean x){boolean getX(){throw new RuntimeException(\"exception in record rewritten method\");}} switch(new R(true)){case R(boolean x) when x: assert false;} assert false;", //
				"exception in record rewritten method");

		// records
		assertFail("record R(boolean x){boolean getX(){throw new RuntimeException(\"exception in record rewritten method\");}} R r = new R(true); boolean x = r.getX();", //
				"exception in record rewritten method");
		assertFail("record R(boolean x){void setX(boolean x){throw new RuntimeException(\"exception in record rewritten method\");}} R r = new R(true);", //
				"exception in record rewritten method");
	}
}