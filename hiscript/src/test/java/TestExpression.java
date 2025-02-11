import org.junit.jupiter.api.Test;

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

		assertSuccessSerialize("byte a = " + Byte.MAX_VALUE + "; assert a == " + Byte.MAX_VALUE + ";");
		assertSuccessSerialize("byte a = " + Byte.MIN_VALUE + "; assert a == " + Byte.MIN_VALUE + ";");
		assertSuccessSerialize("short a = " + Short.MAX_VALUE + "; assert a == " + Short.MAX_VALUE + ";");
		assertSuccessSerialize("short a = " + Short.MIN_VALUE + "; assert a == " + Short.MIN_VALUE + ";");

		assertSuccessSerialize("int a = 2; a /= 2; assert a == 1;");
		assertSuccessSerialize("int a = 1; a *= 2; assert a == 2;");
		assertSuccessSerialize("int a = 1; a += 1; assert a == 2;");
		assertSuccessSerialize("int a = 1; a -= 1; assert a == 0;");
		assertSuccessSerialize("int a = 1; a |= 2; assert a == 3;");
		assertSuccessSerialize("int a = 1 | 2; assert a == 3;");
		assertSuccessSerialize("int a = 3; a &= 2; assert a == 2;");
		assertSuccessSerialize("int a = 3 & 2; assert a == 2;");
		assertSuccessSerialize("int a = 11; a <<= 2; assert a == " + (11 << 2) + ";");
		assertSuccessSerialize("int a = 11; a >>= 2; assert a == " + (11 >> 2) + ";");
		assertSuccessSerialize("int a = 11; a >>>= 2; assert a == " + (11 >>> 2) + ";");
		assertSuccessSerialize("int a = 11>>2; assert a == " + (11 >> 2) + ";");
		assertSuccessSerialize("int a = 11<<2; assert a == " + (11 << 2) + ";");
		assertSuccessSerialize("int a = 11; a ^= 7; assert a == " + (11 ^ 7) + ";");
		assertSuccessSerialize("int a = 11 ^ 7; assert a == " + (11 ^ 7) + ";");
		assertSuccessSerialize("boolean a = true; a &= false; assert !a;");
		assertSuccessSerialize("boolean a = false; a |= true; assert a;");

		assertFailCompile("int a = 1 + true;", //
				"operator '+' can not be applied to int, boolean");
		assertFailCompile("int a = true | 1;", //
				"operator '|' can not be applied to boolean, int");
		assertFailCompile("int a = true;", //
				"incompatible types: boolean cannot be converted to int");
		assertFailCompile("int a = !0;", //
				"operation '!' cannot be applied to 'int'");
		assertFailCompile("boolean a = 1.0;", //
				"incompatible types: double cannot be converted to boolean");
		assertFailCompile("boolean a = +true;", //
				"operation '+' cannot be applied to 'boolean'");
		assertFailCompile("boolean a = -true;", //
				"operation '-' cannot be applied to 'boolean'");
	}

	@Test
	public void testLogicalAndOr() {
		assertFailCompile("if(true && 1){}", //
				"operator '&&' can not be applied to boolean, int");
		assertFailCompile("if(1 && false){}", //
				"operator '&&' can not be applied to int, boolean");
		assertFailCompile("if(1 && 1){}", //
				"operator '&&' can not be applied to int, int");
		assertFailCompile("int x = 1; if(x && true){}", //
				"operator '&&' can not be applied to int, boolean");
		assertFailCompile("var x = 1; if(x && true){}", //
				"operator '&&' can not be applied to int, boolean");
		assertSuccessSerialize("var x = true; if(x && true) {} else {assert false;}");

		assertFailCompile("if(true || 1){}", //
				"operator '||' can not be applied to boolean, int");
		assertFailCompile("if(1 || false){}", //
				"operator '||' can not be applied to int, boolean");
		assertFailCompile("if(1 || 1){}", //
				"operator '||' can not be applied to int, int");
		assertFailCompile("int x = 1; if(x || true){}", //
				"operator '||' can not be applied to int, boolean");
		assertFailCompile("var x = 1; if(x || true){}", //
				"operator '||' can not be applied to int, boolean");
		assertSuccessSerialize("var x = true; if(x || true) {} else {assert false;}");
	}

	@Test
	public void testAllTypesAndOperations() {
		String[] boxNumberTypes = {"Byte", "Short", "Integer", "Long", "Character", "Float", "Double"};
		String[] priNumberTypes = {"byte", "short", "int", "long", "char", "float", "double"};
		String[] boxIntTypes1 = {"Byte", "Short", "Integer", "Long", "Character"};
		String[] priIntTypes1 = {"byte", "short", "int", "long", "char"};
		// float operations
		for (int i = 0; i < boxNumberTypes.length; i++) {
			String bt1 = boxNumberTypes[i];
			String pt1 = priNumberTypes[i];
			for (int j = 0; j < boxNumberTypes.length; j++) {
				String bt2 = boxNumberTypes[j];
				String pt2 = priNumberTypes[j];
				for (String o : new String[] {"+", "-", "*", "/", "%", ">", ">=", "<", "<=", "==", "!="}) {
					// Integer x1=63; byte x2=3; assert (x1 + x2) == ((int)63 + (byte)3);
					assertSuccessSerialize(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccessSerialize(bt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					// assert (new Integer((int)63) + (byte)3) == (63 + 3);
					assertSuccessSerialize("assert (new " + bt1 + "((" + pt1 + ")63) " + o + " (" + pt2 + ")3) == (63 " + o + " 3);");
					assertSuccessSerialize("assert ((" + pt2 + ")63 " + o + " new " + bt1 + "((" + pt1 + ")3)) == (63 " + o + " 3);");
				}
			}
		}
		// int operations
		for (int i = 0; i < boxIntTypes1.length; i++) {
			String bt1 = boxIntTypes1[i];
			String pt1 = priIntTypes1[i];
			for (int j = 0; j < boxIntTypes1.length; j++) {
				String bt2 = boxIntTypes1[j];
				String pt2 = priIntTypes1[j];
				for (String o : new String[] {"&", "|", "^", "<<", ">>", ">>>"}) {
					// Integer x1=63; byte x2=3; assert (x1 + x2) == ((int)63 + (byte)3);
					assertSuccessSerialize(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccessSerialize(bt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					// assert (new Integer((int)63) + (byte)3) == (63 + 3);
					assertSuccessSerialize("assert (new " + bt1 + "((" + pt1 + ")63) " + o + " (" + pt2 + ")3) == (63 " + o + " 3);");
					assertSuccessSerialize("assert ((" + pt2 + ")63 " + o + " new " + bt1 + "((" + pt1 + ")3)) == (63 " + o + " 3);");
				}
			}

			for (String o : new String[] {"-", "+", "~"}) {
				assertSuccessSerialize("assert " + o + "new " + bt1 + "((" + pt1 + ")63) == " + o + "63;");
			}
		}

		// test +=, -= ...
		String[] boxIntTypes2 = {"Integer", "Long"};
		String[] priIntTypes2 = new String[] {"int", "long"};
		for (int i = 0; i < boxIntTypes2.length; i++) {
			String bt1 = boxIntTypes2[i];
			String pt1 = priIntTypes2[i];
			for (int j = 0; j < boxIntTypes1.length; j++) {
				String bt2 = boxIntTypes1[j];
				String pt2 = priIntTypes1[j];
				for (String o : new String[] {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
					assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")63; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccessSerialize(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")63)}; x1[0] " + o + "= (" + pt2 + ")3; assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + "[] x1 = {(" + pt1 + ")63}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")63)}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
				}
			}
		}
		String[] boxIntTypes3 = {"Byte", "Short", "Character"};
		String[] priIntTypes3 = new String[] {"byte", "short", "char"};
		for (int i = 0; i < boxIntTypes3.length; i++) {
			String bt1 = boxIntTypes3[i];
			String pt1 = priIntTypes3[i];
			for (int j = 0; j < boxIntTypes1.length; j++) {
				String bt2 = boxIntTypes1[j];
				String pt2 = priIntTypes1[j];
				for (String o : new String[] {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
					assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + pt2);
					assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + bt2);
					// arrays
					assertFailCompile(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")15)}; x1[0] " + o + "= (" + pt2 + ")3; assert x1[0] == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + pt2);
					assertSuccessSerialize(pt1 + "[] x1 = {(" + pt1 + ")15}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					assertFailCompile(bt1 + " x1[] = {new " + bt1 + "((" + pt1 + ")15)}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + bt2);
				}
			}
		}

		String[] boxIntTypesAll = {"Byte", "Short", "Integer", "Long", "Float", "Double", "Character"};
		String[] priIntTypesALl = {"byte", "short", "int", "long", "float", "double", "char"};
		String[] operations = new String[] {"+", "-", "*", "/", "%"};
		for (int i = 0; i < boxIntTypesAll.length; i++) {
			String bt1 = boxIntTypesAll[i];
			String pt1 = priIntTypesALl[i];
			for (int j = 0; j < boxIntTypesAll.length; j++) {
				String bt2 = boxIntTypesAll[j];
				String pt2 = priIntTypesALl[j];
				for (String o : operations) {
					assertSuccessSerialize(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccessSerialize(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(pt1 + "[][] x1={{63}}; " + bt2 + "[] x2={3}; assert (x1[0][0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccessSerialize(bt1 + "[][][] x1={{{63}}}; " + bt2 + "[] x2={3}; assert (x1[0][0][0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					if (bt1.equals("Byte") || bt1.equals("Short") || bt1.equals("Character")) {
						assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
								"operator '" + o + "=' can not be applied to " + bt1 + ", " + pt2);
					} else {
						assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					}
					assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					if (bt1.equals("Byte") || bt1.equals("Short") || bt1.equals("Character")) {
						assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
								"operator '" + o + "=' can not be applied to " + bt1 + ", " + bt2);
					} else {
						assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					}
				}
			}
		}

		for (String o : new String[] {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
			assertFailCompile("1" + o + "=1;", //
					"variable expected");
			assertFailCompile("this" + o + "=1;", //
					"cannot resolve this");
			assertFailCompile("class A{} A" + o + "=1;", //
					"variable expected");
			assertFailCompile("class A{} new A()" + o + "=1;", //
					"variable expected");
			assertFailCompile("class A{int m(){return 1;}} new A().m()" + o + "=1;", //
					"variable expected");
		}
		assertFailCompile("1++;", //
				"variable expected");
		assertFailCompile("1--;", //
				"variable expected");
		assertFailCompile("++1;", //
				"not a statement");
		assertFailCompile("--1;", //
				"not a statement");
		assertFailCompile("boolean x = !1;", //
				"operation '!' cannot be applied to 'int'");
		assertFailCompile("boolean x = true; x++;", //
				"operation '++' cannot be applied to 'boolean'");
		assertFailCompile("boolean x = true; x--+;", //
				"operation '--' cannot be applied to 'boolean'");
		assertFailCompile("boolean x = true; ++x;", //
				"operation '++' cannot be applied to 'boolean'");
		assertFailCompile("boolean x = true; --x;", //
				"operation '--' cannot be applied to 'boolean'");
		assertFailCompile("String x = \"\"; x++;", //
				"operation '++' cannot be applied to 'String'");
		assertFailCompile("String x = \"\"; x--+;", //
				"operation '--' cannot be applied to 'String'");
		assertFailCompile("String x = \"\"; ++x;", //
				"operation '++' cannot be applied to 'String'");
		assertFailCompile("String x = \"\"; --x;", //
				"operation '--' cannot be applied to 'String'");
		assertFailCompile("float x = ~10f;", //
				"operation '~' cannot be applied to 'float'");
		assertFailCompile("double x = ~10f;", //
				"operation '~' cannot be applied to 'float'");

		for (int i = 0; i < priIntTypesALl.length; i++) {
			String pt = priIntTypesALl[i];
			for (String o : operations) {
				assertFailCompile("var x = (" + pt + ")0 " + o + " new Object();", //
						"operator '" + o + "' can not be applied to " + pt + ", Object");
				assertFailCompile("var x = new Object() " + o + " (" + pt + ")0;", //
						"operator '" + o + "' can not be applied to Object, " + pt);
				assertFailCompile("var x = (" + pt + ")0 " + o + " true;", //
						"operator '" + o + "' can not be applied to " + pt + ", boolean");
				assertFailCompile("var x = false " + o + " (" + pt + ")0;", //
						"operator '" + o + "' can not be applied to boolean, " + pt);
				assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = x" + o + " new Object();", //
						"operator '" + o + "' can not be applied to " + pt + ", Object");
				assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = new Object() " + o + " x;", //
						"operator '" + o + "' can not be applied to Object, " + pt);
				assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = x" + o + " true;", //
						"operator '" + o + "' can not be applied to " + pt + ", boolean");
				assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = true" + o + " x;", //
						"operator '" + o + "' can not be applied to boolean, " + pt);
			}
		}

		// check operations with vars
		for (String t : new String[] {"byte", "short", "int", "long", "float", "double", "char", "Byte", "Short", "Integer", "Long", "Float", "Double", "Character"}) {
			for (String o : new String[] {"+", "-", "*", "/", "%"}) {
				assertSuccessSerialize("interface I{double get(" + t + " x, " + t + " y);} I o = (x, y) -> x " + o + " y;");
			}
		}
		assertSuccessSerialize("interface I{String get(String x, String y);} I o = (x, y) -> x + y;");
		for (String o : new String[] {"|", "||", "&", "&&", "^"}) {
			assertSuccessSerialize("interface I{boolean get(boolean x, boolean y);} I o = (x, y) -> x " + o + " y;");
		}

		assertSuccessSerialize("Integer x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccessSerialize("var x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccessSerialize("Integer x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccessSerialize("var x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");

		assertSuccessSerialize("Boolean x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccessSerialize("var x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccessSerialize("Boolean x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccessSerialize("var x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");

		assertFailCompile("String x = \"\"; int y = x & 1;", //
				"operator '&' can not be applied to String, int");
		assertFailCompile("String x = \"\"; int y = x | 1;", //
				"operator '|' can not be applied to String, int");
		assertFailCompile("String x = \"\"; int y = x ^ 1;", //
				"operator '^' can not be applied to String, int");
		assertFailCompile("Boolean x = true; int y = x & 1;", //
				"operator '&' can not be applied to Boolean, int");
		assertFailCompile("Boolean x = true; int y = x | 1;", //
				"operator '|' can not be applied to Boolean, int");
		assertFailCompile("Boolean x = true; int y = x ^ 1;", //
				"operator '^' can not be applied to Boolean, int");

		for (String o : new String[] {"+", "-", "*", "/", "%", ">", ">=", "<", "<=", "==", "!=", "&", "&&", "|", "||", "^"}) {
			assertFailCompile("boolean x = \"\" " + o + " 1;", //
					o.equals("+") ? "String cannot be converted to boolean" : "operator '" + o + "' can not be applied to String, int");
			assertFailCompile("boolean x = \"\" " + o + " true;", //
					o.equals("+") ? "String cannot be converted to boolean" : "operator '" + o + "' can not be applied to String, boolean");
			assertFailCompile("boolean x = 1 " + o + " true;", //
					"operator '" + o + "' can not be applied to int, boolean");
		}
		assertSuccessSerialize("Boolean x = (boolean) true; assert x;");

		assertSuccessSerialize("boolean x = true; x ^= true;");
		assertSuccessSerialize("int x = 1; x ^= 1;");
		assertFailCompile("boolean x = true; x ^= 1;", //
				"operator '^=' can not be applied to boolean, int");
		assertFailCompile("boolean x = true; x ^= 1.1;", //
				"operator '^=' can not be applied to boolean, double");
		assertFailCompile("int x = 1; x ^= true;", //
				"operator '^=' can not be applied to int, boolean");

		assertFailCompile("int x = 1; x += true;", //
				"operator '+=' can not be applied to int, boolean");
		assertFailCompile("int x = 1; x += \"\";", //
				"operator '+=' can not be applied to int, String");
		assertFailCompile("int x = 1; x -= true;", //
				"operator '-=' can not be applied to int, boolean");
		assertFailCompile("int x = 1; x -= \"\";", //
				"operator '-=' can not be applied to int, String");

		assertSuccessSerialize("int x = 2; x >>=1; assert x == 1;");
		assertFailCompile("int x = 2; x >>=true;", //
				"operator '>>=' can not be applied to int, boolean");
		assertSuccessSerialize("int x = 1; x <<=1; assert x == 2;");
		assertFailCompile("int x = 2; x <<=true;", //
				"operator '<<=' can not be applied to int, boolean");
		assertFailCompile("int x = 1; x <<<=1;", //
				"invalid expression");
		assertSuccessSerialize("int x = 2; x >>>=1; assert x == 1;");
		assertFailCompile("int x = 2; x >>>=true;", //
				"operator '>>>=' can not be applied to int, boolean");
		assertFailCompile("int x = 1 >> true;", //
				"operator '>>' can not be applied to int, boolean");
		assertFailCompile("int x = 1 << true;", //
				"operator '<<' can not be applied to int, boolean");
		assertFailCompile("int x = 1 >>> true;", //
				"operator '>>>' can not be applied to int, boolean");

		// divide
		assertFailCompile("double x = 1; x *= true;", //
				"operator '*=' can not be applied to double, boolean");
		assertFailCompile("double x = 1; x /= true;", //
				"operator '/=' can not be applied to double, boolean");
		assertFailCompile("double x = 1; x %= true;", //
				"operator '%=' can not be applied to double, boolean");
		assertFailCompile("boolean x = true; x *= 1;", //
				"operator '*=' can not be applied to boolean, int");
		assertFailCompile("boolean x = true; x /= 1;", //
				"operator '/=' can not be applied to boolean, int");
		assertFailCompile("boolean x = true; x %= 1;", //
				"operator '%=' can not be applied to boolean, int");

		// equals
		assertSuccessSerialize("var x = true; var y = true; assert x == y;");
		assertSuccessSerialize("var x = true; var y = false; assert x != y;");

		// null
		assertSuccessSerialize("Object x = null; assert null == x;");
		assertSuccessSerialize("Object x = null; assert x == null;");
		assertSuccessSerialize("assert null == null;");
		assertSuccessSerialize("Object x = null; assert x != \"\";");
		assertSuccessSerialize("Object x = null; assert \"\" != x;");
		assertSuccessSerialize("assert null != \"\";");
		assertSuccessSerialize("assert \"\" != null;");

		// zero
		assertFail("double x = 1; x /= (byte)0;", //
				"divide by zero");
		assertFail("double x = 1; x /= (short)0;", //
				"divide by zero");
		assertFail("double x = 1; x /= (char)0;", //
				"divide by zero");
		assertFail("double x = 1; x /= 0;", //
				"divide by zero");
		assertFail("double x = 1; x /= 0L;", //
				"divide by zero");
		assertFail("double x = 1; x /= 0f;", //
				"divide by zero");
		assertFail("double x = 1; x /= 0.0;", //
				"divide by zero");

		assertFail("double x = 1 / (byte)0;", //
				"divide by zero");
		assertFail("double x = 1 / (short)0;", //
				"divide by zero");
		assertFail("double x = 1 / (char)0;", //
				"divide by zero");
		assertFail("double x = 1 / 0;", //
				"divide by zero");
		assertFail("double x = 1 / 0L;", //
				"divide by zero");
		assertFail("double x = 1 / 0f;", //
				"divide by zero");
		assertFail("double x = 1 / 0.0;", //
				"divide by zero");

		assertFail("double x = 1; byte y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; short y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; char y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; int y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; long y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; float y = 0; double z = x / y;", //
				"divide by zero");
		assertFail("double x = 1; double y = 0; double z = x / y;", //
				"divide by zero");
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
		assertSuccessSerialize("assert ~~ ~~ ~~ ~~ ~~123 == 123;");
		assertSuccessSerialize("assert ~~~123 == ~123;");
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
								assertSuccessSerialize(script);
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
		assertSuccessSerialize("interface A{} interface B extends A{} class C implements B{} assert new C() instanceof A; A a = new C();");
		assertSuccessSerialize("Integer x = null; if(x instanceof Integer){assert false;}");

		assertFailCompile("int x = 1; if(x instanceof Integer){}", //
				"inconvertible types; cannot cast int to Integer");
		assertFailCompile("Integer x = 1; if(x instanceof int){}", //
				"unexpected token");
		assertFailCompile("Integer x; if(x instanceof Integer){}", //
				"variable 'x' is not initialized");
		assertFailCompile("Integer x = 1; if(x instanceof 1){}", //
				"type expected");
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

		assertFailCompile("char c1 = -1;", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("char c1 = 'a'; char c2 = c1 + '1';", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("char c1 = 'a'; char c2 = c1 + 1;", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("char c1 = 'a'; char c2 = c1 + 1l;", //
				"incompatible types: long cannot be converted to char");
		assertFailCompile("char c1 = 'a'; char c2 = c1 + (byte)1;", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("char c1 = 'a'; char c2 = c1 + (short)1;", //
				"incompatible types: int cannot be converted to char");

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

		assertFailCompile("Long x = true ? \"\" : new Integer(1);", //
				"incompatible types: Object cannot be converted to Long");
		assertFailCompile("int x = true ? 1 : \"\";", //
				"incompatible switch values types: 'int' and 'String'");
		assertFailCompile("int x = 1 ? 1 : 2;", //
				"boolean is expected");
		assertFailCompile("int x = 1 ? true : 2;", //
				"boolean is expected");
		assertFailCompile("int x = true ? ;", //
				"expression expected");
		assertFailCompile("int x = true ? 1;", //
				"':' is expected");
		assertFailCompile("int x = true ? 1 : ;", //
				"expression expected");
		assertFailCompile("int x = true ? : 1;", //
				"expression expected");
		assertFailCompile("int x = true ? : ;", //
				"expression expected");
	}

	@Test
	public void testIncrements() {
		assertSuccessSerialize("int x = 1; assert x++ + x++ == 3;");
		assertSuccessSerialize("int x = 1; assert ++x + ++x == 5;");
		assertSuccessSerialize("int x = 1; int y = ++x + x++ + x; assert x == 3; assert y == 7;");

		assertSuccessSerialize("int x = -1; assert x-- + x-- == -3;");
		assertSuccessSerialize("int x = -1; assert --x + --x == -5;");
		assertSuccessSerialize("int x = -1; int y = --x + x-- + x; assert x == -3; assert y == -7;");

		for (String t : new String[] {"byte", "short", "char", "int", "long", "float", "double"}) {
			assertSuccessSerialize(t + " x = 1; x++; assert x == 2;");
			assertSuccessSerialize(t + " x = 1; x--; assert x == 0;");
			assertSuccessSerialize(t + " x = 1; ++x; assert x == 2;");
			assertSuccessSerialize(t + " x = 1; --x; assert x == 0;");
		}

		assertFailCompile("String s = \"\"; s++;", //
				"operation '++' cannot be applied to 'String'");
		assertFailCompile("String s = \"\"; s--;", //
				"operation '--' cannot be applied to 'String'");
		assertFailCompile("String s = \"\"; ++s;", //
				"operation '++' cannot be applied to 'String'");
		assertFailCompile("String s = \"\"; --s;", //
				"operation '--' cannot be applied to 'String'");
		assertFailCompile("boolean s = true; s++;", //
				"operation '++' cannot be applied to 'boolean'");
		assertFailCompile("boolean s = true; s--;", //
				"operation '--' cannot be applied to 'boolean'");
		assertFailCompile("boolean s = true; ++s;", //
				"operation '++' cannot be applied to 'boolean'");
		assertFailCompile("boolean s = true; --s;", //
				"operation '--' cannot be applied to 'boolean'");
	}

	@Test
	public void testBoolean() {
		assertSuccessSerialize("boolean x = !true; assert !x;");
		assertSuccessSerialize("boolean x = !!true; assert x;");
		assertSuccessSerialize("boolean x = !! !! !! !! !! true; assert x;");
		assertSuccessSerialize("boolean x = !false; assert x;");
		assertSuccessSerialize("boolean x = true; assert !x == false; assert !!x == true; assert !!!x == false; assert !!!!x == true; assert !!!!!x == false;");
	}

	@Test
	public void testObject() {
		assertSuccessSerialize("Object o1 = new Object(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
		assertSuccessSerialize("class O{} O o1 = new O(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
		assertSuccessSerialize("class O extends Object{} O o1 = new O(); Object o2 = o1; O o3 = (O)o2;");
		assertSuccessSerialize("class O{int x = 0; public boolean equals(Object o){return x == ((O)o).x;}} O o1 = new O(); O o2 = new O(); assert o1 != o2; assert o1.equals(o2); o2.x++; assert !o1.equals(o2);");
		assertSuccessSerialize("assert new Object(){int m(int x){return x;}}.m(123) == 123;");
		assertSuccessSerialize("var o = new Object(){void m(){}}; o.m();");

		assertFailCompile("class O{} O o1 = new O(); Object o2 = o1; O o3 = o2;", //
				"incompatible types: Object cannot be converted to O");
		assertFailCompile("class O extends O{}", //
				"cyclic inheritance involving O");
		assertFailCompile("interface I extends I{}", //
				"cyclic inheritance involving I");
		assertFailCompile("Object o = new Object(){void m(){}}; o.m();", //
				"cannot resolve method 'm' in 'Object'");
	}

	@Test
	public void variableWithoutName() {
		assertSuccessSerialize("String[] arr = {\"a\", \"b\", \"c\"}; int i = 0; for(String _ : arr) i++; assert i == arr.length;");
	}

	@Test
	public void testSwitch() {
		assertSuccessSerialize("int x = 1; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 10;");
		assertSuccessSerialize("int x = 2; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 20;");
		assertSuccessSerialize("int x = 3; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 30;");

		assertSuccessSerialize("String x = null; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 0;");
		assertSuccessSerialize("String x = \"a\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 1;");
		assertSuccessSerialize("String x = \"b\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 1;");
		assertSuccessSerialize("String x = \"c\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 2;");

		assertSuccessSerialize("int x = switch(\"a\"){case String s when s.length() == 1 -> 1; case String s when s.length() == 0 -> 2;}; assert x == 1;");
		assertSuccessSerialize("class O{int x; O(int x){this.x = x;}} int x = switch(new O(2)){case O o when o.x == 1 -> 1; case O o when o.x == 2 -> o.x + 1;} assert x == 3;");

		// TODO
		// assertFailCompile("int x = 2; int y = switch(x){case 1 -> 10; case 2 -> 20;};"); // not all cases
		// assertFailCompile("String x = \"c\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; case \"c\" -> 2;};"); // not all cases
		assertFailCompile("int x = 1; int y = switch(x){case 1 -> 10; case 2 -> true;};", //
				"incompatible switch values types; found boolean, required int");
		assertFailCompile("int x = switch(1){case 1 -> 1; case \"\" -> 2;};", //
				"incompatible switch case types; found String, required int");

		// failures
		assertFailCompile("switch(1){case 1 -> 1;};", //
				"':' is expected");
		assertFailCompile("int x = switch(){case 1 -> 1;};", //
				"expression expected");
		assertFailCompile("int x = switch(1){case 1 -> ;};", //
				"expression expected");
		assertFailCompile("int x = switch(1){case 1;};", //
				"'->' is expected");
		assertFailCompile("int x = switch(1){case 1 -> {};};", //
				"expression expected");
		assertFailCompile("int x = switch(1){case -> 1;};", //
				"empty case value");
		assertFailCompile("int x = switch(1){case 1 -> 1; default;};", //
				"'->' is expected");
		assertFailCompile("int x = switch(1){case 1 -> 1; default ->;};", //
				"expression expected");
		assertFailCompile("int x = switch(1){case 1 -> 1; default -> \"\";};", //
				"incompatible switch values types; found String, required int");
		assertFailCompile("String s = \"\"; int x = switch(1){case s -> 1;};", //
				"'->' is expected");
		assertFailCompile("int x = switch(1){case 1 -> \"a\"; case 2 -> \"b\";};", //
				"incompatible types: String cannot be converted to int");
		assertFailCompile("int x = switch(1){};", //
				"expression switch without cases");
		assertFail("int x = switch(1){case 2 -> 2;};", //
				"no suitable value in the switch");

		assertFailCompile("int x = switch(\"\"){case String s, Integer i -> 1;};", //
				"only one casted identifier is allowed in the case condition");
		assertFailCompile("int x = switch(\"\"){case String -> 1;};", //
				"'->' is expected");
		assertFailCompile("int x = switch(\"\"){case x -> 1;};", //
				"invalid expression");
		assertFailCompile("class A{static void a(){}} int x = switch(\"\"){case \"\" -> A.a();};", //
				"value is expected");
		assertFailCompile("class A{static void a(){}} int x = switch(\"\"){case A.a() -> 1;};", //
				"value or casted identifier is expected");
	}

	@Test
	public void testCast() {
		String[] types = new String[] {"byte", "short", "char", "int", "long", "float", "double"};
		for (String t : types) {
			assertSuccessSerialize(t + " x = (" + t + ")127.0; assert x == 127;");
			for (String t2 : types) {
				assertSuccessSerialize(t + " x = (" + t + ")(" + t2 + ")127; assert x == 127;");
			}
			assertSuccessSerialize(t + " x = (" + t + ")(double)(float)(char)(long)(byte)127.0; assert x == 127;");
			assertFailCompile(t + " x = (boolean)(byte)1;", //
					"cannot cast byte to boolean");
			assertFailCompile(t + " x = (" + t + ")\"1\";", //
					"cannot cast String to " + t);
			assertFailCompile(t + " x = (" + t + ")true;", //
					"cannot cast boolean to " + t);
			assertFailCompile(t + " x = (" + t + ");", //
					"expression expected");
			assertFailCompile(t + " x = (" + t + ")new int[0];", //
					"cannot cast int[] to " + t);
			assertFailCompile(t + " x = (" + t + ")new String[1];", //
					"cannot cast String[] to " + t);
			assertFailCompile(t + " x = (void)1;", //
					"expression expected");
			assertFailCompile("String s = (String)(" + t + ")1;", //
					"cannot cast " + t + " to String");
			assertFailCompile("boolean b = (boolean)(" + t + ")1;", //
					"cannot cast " + t + " to boolean");
			assertFailCompile("String s = (" + t + ")\"\";", //
					"cannot cast String to " + t);
			assertFailCompile("void s = (" + t + ")1;", //
					"unexpected token");
			assertFailCompile("boolean b = (" + t + ")true;", //
					"cannot cast boolean to " + t);
			assertFailCompile("class A{} " + t + " x = (" + t + ")A;", //
					"cannot cast A to " + t);

			assertSuccessSerialize(t + "[] x = (" + t + "[])new " + t + "[]{127}; assert x[0] == 127;");
			for (String t2 : new String[] {"byte", "short", "int", "long", "float", "double", "char"}) {
				if (!t.equals(t2)) {
					assertFailCompile(t + "[] x = (" + t2 + "[])new " + t + "[]{127}; assert x[0] == 127;", //
							"cannot cast " + t + "[] to " + t2 + "[]");
					assertFailCompile(t + "[] x = (" + t + "[])new " + t2 + "[]{127}; assert x[0] == 127;", //
							"cannot cast " + t2 + "[] to " + t + "[]");
				}
			}
		}

		// autocast
		assertSuccessSerialize("byte x = 1; assert x == (byte)1;");
		assertSuccessSerialize("byte x = 'a'; assert x == (byte)'a';");
		assertFailCompile("byte x = 1l;", //
				"incompatible types: long cannot be converted to byte");
		assertFailCompile("byte x = 1f;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("byte x = 1d;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte x = 129;", //
				"incompatible types: int cannot be converted to byte");

		assertSuccessSerialize("short x = 1; assert x == (short)1;");
		assertSuccessSerialize("short x = 'a'; assert x == (short)'a';");
		assertFailCompile("short x = 1l;", //
				"incompatible types: long cannot be converted to short");
		assertFailCompile("short x = 1f;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("short x = 1d;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short x = " + (Short.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to short");

		assertSuccessSerialize("int x = 1; assert x == (int)1;");
		assertSuccessSerialize("int x = 'a'; assert x == (int)'a';");
		assertFailCompile("int x = 1l;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("int x = 1f;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int x = 1d;", //
				"incompatible types: double cannot be converted to int");

		assertSuccessSerialize("long x = 1; assert x == 1L;");
		assertSuccessSerialize("long x = 'a'; assert x == (long)'a';");
		assertFailCompile("long x = 1f;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long x = 1d;", //
				"incompatible types: double cannot be converted to long");

		assertSuccessSerialize("float x = 1; assert x == 1f;");
		assertSuccessSerialize("float x = 'a'; assert x == (float)'a';");
		assertFailCompile("float x = 1d;", //
				"incompatible types: double cannot be converted to float");

		assertSuccessSerialize("double x = 1; assert x == 1d;");
		assertSuccessSerialize("double x = 'a'; assert x == (double)'a';");
		assertSuccessSerialize("double x = 1L; assert x == 1d;");
		assertSuccessSerialize("double x = 1f; assert x == 1d;");

		// assignments
		assertSuccessSerialize("byte a = 1; byte x = a; assert x == 1;");
		assertFailCompile("short a = 1; byte x = a; assert x == 1;", //
				"incompatible types: short cannot be converted to byte");
		assertFailCompile("char a = 1; byte x = a; assert x == 1;", //
				"incompatible types: char cannot be converted to byte");
		assertFailCompile("int a = 1; byte x = a; assert x == 1;", //
				"incompatible types: int cannot be converted to byte");
		assertFailCompile("long a = 1; byte x = a; assert x == 1;", //
				"incompatible types: long cannot be converted to byte");
		assertFailCompile("float a = 1; byte x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("double a = 1; byte x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to byte");

		assertSuccessSerialize("byte a = 1; short x = a; assert x == 1;");
		assertSuccessSerialize("short a = 1; short x = a; assert x == 1;");
		assertFailCompile("char a = 1; short x = a; assert x == 1;", //
				"incompatible types: char cannot be converted to short");
		assertFailCompile("int a = 1; short x = a; assert x == 1;", //
				"incompatible types: int cannot be converted to short");
		assertFailCompile("long a = 1; short x = a; assert x == 1;", //
				"incompatible types: long cannot be converted to short");
		assertFailCompile("float a = 1; short x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("double a = 1; short x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to short");

		assertFailCompile("byte a = 1; char x = a; assert x == 1;", //
				"incompatible types: byte cannot be converted to char");
		assertFailCompile("short a = 1; char x = a; assert x == 1;", //
				"incompatible types: short cannot be converted to char");
		assertSuccessSerialize("char a = 1; char x = a; assert x == 1;");
		assertFailCompile("int a = 1; char x = a; assert x == 1;", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("long a = 1; char x = a; assert x == 1;", //
				"incompatible types: long cannot be converted to char");
		assertFailCompile("float a = 1; char x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to char");
		assertFailCompile("double a = 1; char x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to char");

		assertSuccessSerialize("byte a = 1; int x = a; assert x == 1;");
		assertSuccessSerialize("short a = 1; int x = a; assert x == 1;");
		assertSuccessSerialize("char a = 1; int x = a; assert x == 1;");
		assertSuccessSerialize("int a = 1; int x = a; assert x == 1;");
		assertFailCompile("long a = 1; int x = a;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("float a = 1; int x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("double a = 1; int x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to int");

		assertSuccessSerialize("byte a = 1; long x = a; assert x == 1L;");
		assertSuccessSerialize("short a = 1; long x = a; assert x == 1L;");
		assertSuccessSerialize("char a = 1; long x = a; assert x == 1L;");
		assertSuccessSerialize("int a = 1; long x = a; assert x == 1L;");
		assertSuccessSerialize("long a = 1; long x = a; assert x == 1L;");
		assertFailCompile("float a = 1; long x = a; assert x == 1L;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("double a = 1; long x = a; assert x == 1L;", //
				"incompatible types: double cannot be converted to long");

		assertSuccessSerialize("byte a = 1; float x = a; assert x == 1F;");
		assertSuccessSerialize("short a = 1; float x = a; assert x == 1F;");
		assertSuccessSerialize("char a = 1; float x = a; assert x == 1F;");
		assertSuccessSerialize("int a = 1; float x = a; assert x == 1F;");
		assertSuccessSerialize("long a = 1; float x = a; assert x == 1F;");
		assertSuccessSerialize("float a = 1; float x = a; assert x == 1F;");
		assertFailCompile("double a = 1; float x = a; assert x == 1F;", //
				"incompatible types: double cannot be converted to float");

		assertSuccessSerialize("byte a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("short a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("char a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("int a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("long a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("float a = 1; double x = a; assert x == 1D;");
		assertSuccessSerialize("double a = 1; double x = a; assert x == 1D;");
	}

	@Test
	public void testNotEquals() {
		assertSuccessSerialize("assert 1 != 2;");
		assertSuccessSerialize("assert 1.0 != 2.0;");
		assertSuccessSerialize("assert 1f != 2.0;");
		assertSuccessSerialize("assert 1 != 1.1f;");
		assertSuccessSerialize("assert true != false;");

		assertSuccessSerialize("assert \"a\" != \"b\";");
		assertSuccessSerialize("assert new Integer(1) != new Integer(2);");

		assertSuccessSerialize("var x = 1; var y = 2; assert x != y;");
		assertSuccessSerialize("var x = 1; int y = 2; assert x != y;");

		assertFailCompile("assert true != 2;", //
				"operator '!=' can not be applied to boolean, int");
		assertFailSerialize("assert \"a\" != \"a\";");
		assertSuccessSerialize("assert new Integer(1) != new Integer(1);");

		// equals
		assertSuccessSerialize("assert \"a\" == \"a\";");
		assertFailSerialize("assert \"a\" == \"b\";");
	}

	@Test
	public void testArrays() {
		assertSuccessSerialize("int[] x = {2}; x[0] *= 2; assert x[0] == 4;");
	}

	@Test
	public void testGetAndSet() {
		for (String t : new String[] {"byte", "short", "int", "long", "float", "double"}) {
			assertSuccessSerialize("byte x = 1; " + t + " y = x;");
		}
		for (String t : new String[] {"short", "int", "long", "float", "double"}) {
			assertSuccessSerialize("short x = 1; " + t + " y = x;");
		}
		for (String t : new String[] {"int", "long", "float", "double"}) {
			assertSuccessSerialize("int x = 1; " + t + " y = x;");
		}
	}
}
