import org.junit.jupiter.api.Test;

public class TestExpression extends HiTest {
	@Test
	public void testSimpleNumberExpressions() {
		assertSuccess("int x = 1 + 100_000_000;");

		assertSuccess("int a = 1 + 2; assert a == 3;");
		assertSuccess("int a = 10 - 1; assert a == 9;");
		assertSuccess("int a = 5 * 6; assert a == 30;");
		assertSuccess("int a = 121 / 11; assert a == 11;");
		assertSuccess("int a = 10 % 9; assert a == 1;");

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

		assertSuccess("int a = 2; a /= 2; assert a == 1;");
		assertSuccess("int a = 1; a *= 2; assert a == 2;");
		assertSuccess("int a = 1; a += 1; assert a == 2;");
		assertSuccess("int a = 1; a -= 1; assert a == 0;");
		assertSuccess("int a = 1; a |= 2; assert a == 3;");
		assertSuccess("int a = 1 | 2; assert a == 3;");
		assertSuccess("int a = 3; a &= 2; assert a == 2;");
		assertSuccess("int a = 3 & 2; assert a == 2;");
		assertSuccess("int a = 11; a <<= 2; assert a == " + (11 << 2) + ";");
		assertSuccess("int a = 11; a >>= 2; assert a == " + (11 >> 2) + ";");
		assertSuccess("int a = 11; a >>>= 2; assert a == " + (11 >>> 2) + ";");
		assertSuccess("int a = 11>>2; assert a == " + (11 >> 2) + ";");
		assertSuccess("int a = 11<<2; assert a == " + (11 << 2) + ";");
		assertSuccess("int a = 11; a ^= 7; assert a == " + (11 ^ 7) + ";");
		assertSuccess("int a = 11 ^ 7; assert a == " + (11 ^ 7) + ";");
		assertSuccess("boolean a = true; a &= false; assert !a;");
		assertSuccess("boolean a = false; a |= true; assert a;");

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
		assertSuccess("var x = true; if(x && true) {} else {assert false;}");

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
		assertSuccess("var x = true; if(x || true) {} else {assert false;}");
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
					assertSuccess(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccess(bt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					// assert (new Integer((int)63) + (byte)3) == (63 + 3);
					assertSuccess("assert (new " + bt1 + "((" + pt1 + ")63) " + o + " (" + pt2 + ")3) == (63 " + o + " 3);");
					assertSuccess("assert ((" + pt2 + ")63 " + o + " new " + bt1 + "((" + pt1 + ")3)) == (63 " + o + " 3);");
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
					assertSuccess(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccess(bt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + "[] x1={63}; " + bt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					// assert (new Integer((int)63) + (byte)3) == (63 + 3);
					assertSuccess("assert (new " + bt1 + "((" + pt1 + ")63) " + o + " (" + pt2 + ")3) == (63 " + o + " 3);");
					assertSuccess("assert ((" + pt2 + ")63 " + o + " new " + bt1 + "((" + pt1 + ")3)) == (63 " + o + " 3);");
				}
			}

			for (String o : new String[] {"-", "+", "~"}) {
				assertSuccess("assert " + o + "new " + bt1 + "((" + pt1 + ")63) == " + o + "63;");
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
					assertSuccess(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + " x1 = (" + pt1 + ")63; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccess(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")63)}; x1[0] " + o + "= (" + pt2 + ")3; assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + "[] x1 = {(" + pt1 + ")63}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")63)}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
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
					assertSuccess(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + bt2);
					// arrays
					assertFailCompile(bt1 + "[] x1 = {new " + bt1 + "((" + pt1 + ")15)}; x1[0] " + o + "= (" + pt2 + ")3; assert x1[0] == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
							"operator '" + o + "=' can not be applied to " + bt1 + ", " + pt2);
					assertSuccess(pt1 + "[] x1 = {(" + pt1 + ")15}; x1[0] " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1[0] == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
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
					assertSuccess(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					// arrays
					assertSuccess(bt1 + "[] x1={63}; " + pt2 + "[] x2={3}; assert (x1[0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(pt1 + "[][] x1={{63}}; " + bt2 + "[] x2={3}; assert (x1[0][0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
					assertSuccess(bt1 + "[][][] x1={{{63}}}; " + bt2 + "[] x2={3}; assert (x1[0][0][0] " + o + " x2[0]) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

					if (bt1.equals("Byte") || bt1.equals("Short") || bt1.equals("Character")) {
						assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
								"operator '" + o + "=' can not be applied to " + bt1 + ", " + pt2);
					} else {
						assertSuccess(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					}
					assertSuccess(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
					if (bt1.equals("Byte") || bt1.equals("Short") || bt1.equals("Character")) {
						assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);", //
								"operator '" + o + "=' can not be applied to " + bt1 + ", " + bt2);
					} else {
						assertSuccess(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
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
		assertFailCompile("!true;", //
				"not a statement");
		assertFailCompile("1++;", //
				"variable expected");
		assertFailCompile("1--;", //
				"variable expected");
		assertFailCompile("++1;", // TODO variable expected, operations = [null] ???
				"not a statement");
		assertFailCompile("--1;", // TODO variable expected
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
				assertSuccess("interface I{double get(" + t + " x, " + t + " y);} I o = (x, y) -> x " + o + " y;");
			}
		}
		assertSuccess("interface I{String get(String x, String y);} I o = (x, y) -> x + y;");
		for (String o : new String[] {"|", "||", "&", "&&", "^"}) {
			assertSuccess("interface I{boolean get(boolean x, boolean y);} I o = (x, y) -> x " + o + " y;");
		}

		assertSuccess("Integer x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("var x = 2; Integer y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("Integer x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");
		assertSuccess("var x = 2; var y = 3; assert (x & y) == 2; assert (x | y) == 3; assert (x ^ y) == 1;");

		assertSuccess("Boolean x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("var x = true; Boolean y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("Boolean x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");
		assertSuccess("var x = true; var y = false; assert (x & y) == false; assert (x | y) == true; assert (x ^ y) == true;");

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
		assertSuccess("Boolean x = (boolean) true; assert x;");

		assertSuccess("boolean x = true; x ^= true;");
		assertSuccess("int x = 1; x ^= 1;");
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

		assertSuccess("int x = 2; x >>=1; assert x == 1;");
		assertFailCompile("int x = 2; x >>=true;", //
				"operator '>>=' can not be applied to int, boolean");
		assertSuccess("int x = 1; x <<=1; assert x == 2;");
		assertFailCompile("int x = 2; x <<=true;", //
				"operator '<<=' can not be applied to int, boolean");
		assertFailCompile("int x = 1; x <<<=1;", //
				"invalid expression");
		assertSuccess("int x = 2; x >>>=1; assert x == 1;");
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
		assertFail("byte x = 1; x /= (byte)0;", //
				"divide by zero");
		assertFail("short x = 1; x /= (short)0;", //
				"divide by zero");
		assertFail("char x = 1; x /= (char)0;", //
				"divide by zero");
		assertFail("int x = 1; x /= 0;", //
				"divide by zero");
		assertFail("long x = 1; x /= 0L;", //
				"divide by zero");
		assertSuccess("float x = 1; x /= 0f; assert Float.isInfinite(x);");
		assertSuccess("double x = 1; x /= 0.0; assert Double.isInfinite(x);");

		for (String t1 : new String[] {"int", "long"}) {
			for (String t2 : new String[] {"byte", "short", "char", "int"}) {
				assertFail(t1 + " x = 1 / (" + t2 + ")0;", //
						"divide by zero");
			}
		}
		for (String t1 : new String[] {"byte", "short", "char", "int", "long"}) {
			for (String t2 : new String[] {"byte", "short", "char", "int", "long"}) {
				assertFail("float x = (" + t1 + ") 1 / (" + t2 + ")0;", //
						"divide by zero");
			}
		}
		for (String t1 : new String[] {"byte", "short", "char", "int", "long"}) {
			for (String t2 : new String[] {"byte", "short", "char", "int", "long"}) {
				assertFail("double x =  (" + t1 + ") 1 / (" + t2 + ")0;", //
						"divide by zero");
			}
		}
		assertSuccess("float x = 1; x /= 0f; assert Float.isInfinite(x);");
		assertSuccess("double x = 1; x /= 0f; assert Double.isInfinite(x);");
		assertSuccess("double x = 1; x /= 0d; assert Double.isInfinite(x);");

		for (String t1 : new String[] {"byte", "short", "char", "int", "long", "float", "double"}) {
			for (String t2 : new String[] {"byte", "short", "char", "int", "long", "float", "double"}) {
				if (t1.equals("float") || t1.equals("double") || t2.equals("float") || t2.equals("double")) {
					if (!t1.equals("double") && !t2.equals("double")) {
						assertSuccess(t1 + " x = 1; " + t2 + " y = 0; float z = x / y; Float.isInfinite(x);");
					}
					assertSuccess(t1 + " x = 1; " + t2 + " y = 0; double z = x / y; Double.isInfinite(x);");
				} else {
					assertFail(t1 + " x = 1; " + t2 + " y = 0; double z = x / y;", //
							"divide by zero");
				}
			}
		}
	}

	@Test
	public void testComplexNumberExpressions() {
		assertCondition("int a = (10 + 5) & 7; System.println(\"a=\" + a);", "a == 7", "expression 2");
		assertCondition("int a = (2 - +1) * +-+ + -2 / - + - + +2 /*=1*/    +    (((1))) * (2 -1/(int)1 + 1*0) /*=1*/    -    16/2/2/2 /*=2*/; System.println(\"a=\" + a);", "a == 0", "expression 1");
		assertSuccess("assert (1 + (int)1.1 + 1) == 3;");
		assertSuccess("assert (1 + (int)2.1 / (byte)2 + 1) == 3;");
		assertSuccess("assert ((int)1.9f + (int)1 - (float)2) == 0.0;");
		assertSuccess("assert ((int)1.9f + (float)(int)3 / (float)2) == 2.5;");
		assertSuccess("int x = 5; assert (- -+ -x + - (int) + (float) - + + + + +(double)x) == 0;");
		assertSuccess("assert 2 * 6 / 3 + 4 * 5 / (20 - 9 * 2) / 2 == 9;");
		assertSuccess("assert (1 >= 0 ? 1 + 2 : 2 + 3) == 3;");
		assertSuccess("assert (1 > 2 ? 3 > 4 ? 5 : 6 : 7 > 8 ? 9 : 10) == 10;");
		assertSuccess("assert 16/-2/2/-2/+2*2/2 == 1;");
		assertSuccess("assert 2/+ -2 == -100000000000L/100000000000L;");
		assertSuccess("assert ~~ ~~ ~~ ~~ ~~123 == 123;");
		assertSuccess("assert ~~~123 == ~123;");
	}

	@Test
	public void testNumbersCasts() {
		String[] prefixes1 = {" ", "(byte)", "(short)", "(int)", "(long)", "(double)", "(float)", "(char)"};
		String[] prefixes2 = {"+", "- -", "+ +", " "};
		String[] suffixes = {"", ".", ".0", ".0000", "f", "F", "d", "D", "l", "L", ".d", ".D", ".0d", ".0D", "e+0", "e-0", "0e-1"};
		testSuccessNumbersCasts(prefixes1, prefixes2, suffixes, new String[] {"1", "127"});

		assertSuccess("assert (byte)" + (Byte.MAX_VALUE + 1) + " == " + (byte) (Byte.MAX_VALUE + 1) + ";");
		assertSuccess("assert (byte)" + (Byte.MIN_VALUE - 1) + " == " + (byte) (Byte.MIN_VALUE - 1) + ";");
		assertSuccess("assert (short)" + (Short.MAX_VALUE + 1) + " == " + (short) (Short.MAX_VALUE + 1) + ";");
		assertSuccess("assert (short)" + (Short.MIN_VALUE - 1) + " == " + (short) (Short.MIN_VALUE - 1) + ";");
		assertSuccess("assert (int)" + (Integer.MAX_VALUE + 1l) + "L == " + (int) (Integer.MAX_VALUE + 1l) + "L;");
		assertSuccess("assert ( int )" + (Integer.MIN_VALUE - 1l) + "L == " + (int) (Integer.MIN_VALUE - 1l) + ";");
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
		assertSuccess("assert \"\" instanceof String;");
		assertSuccess("assert new Object() instanceof Object;");
		assertSuccess("assert !(null instanceof String);");
		assertSuccess("assert !(null instanceof Object);");
		assertSuccess("String s = \"\"; assert s instanceof Object;");
		assertSuccess("String s = null; assert !(s instanceof String);");
		assertSuccess("String s = null; assert !(s instanceof Object);");
		assertSuccess("Object s = new Object(); assert s instanceof Object;");
		assertSuccess("Object s = \"\"; assert s instanceof Object;");
		assertSuccess("Object s = \"\"; assert s instanceof String;");
		assertSuccess("Object s = null; assert !(s instanceof Object);");
		assertSuccess("class O1{}; class O2 extends O1{}; class O3 extends O2{}; class O4 extends O3{};" + //
				"assert new O2() instanceof O2;" + //
				"assert new O2() instanceof O1;" + //
				"assert !(new O1() instanceof O2);" + //
				"assert new O4() instanceof O1;");

		assertSuccess("class A {int a = 1;} class B extends A{int b = 2;}" + //
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
		assertSuccess("interface A{} interface B extends A{} class C implements B{} assert new C() instanceof A; A a = new C();");
		assertSuccess("Integer x = null; if(x instanceof Integer){assert false;}");

		assertFailCompile("int x = 1; if(x instanceof Integer){}", //
				"inconvertible types; cannot cast int to Integer");
		assertFailCompile("Integer x = 1; if(x instanceof int){}", //
				"inconvertible types; cannot cast Integer to int");
		assertFailCompile("Integer x; if(x instanceof Integer){}", //
				"variable 'x' is not initialized");
		assertFailCompile("Integer x = 1; if(x instanceof 1){}", //
				"type expected");

		assertSuccess("int[] x = {}; assert x instanceof int[];");
		assertFailCompile("int[] x = {}; boolean b = x instanceof Integer;", //
				"inconvertible types; cannot cast int[] to Integer");
		assertSuccess("Object x = new int[]{}; assert !(x instanceof Integer);");
		assertFailCompile("int[] x = {}; boolean b = x instanceof double[];", //
				"inconvertible types; cannot cast int[] to double[]");
		assertSuccess("Object x = new int[]{}; assert !(x instanceof double[]);");
		assertSuccess("Object x = new int[]{}; assert !(x instanceof int[][]);");
		assertSuccess("String[] x = {}; assert x instanceof String[];");
		assertSuccess("String[] x = new String[]{}; assert x instanceof Object[];");
		assertFailCompile("String[] x = new String[]{}; boolean b = x instanceof String[][];", //
				"inconvertible types; cannot cast String[] to String[][]");
		assertSuccess("Object[] x = {}; assert x instanceof Object[];");
		assertSuccess("Object[] x = {}; assert !(x instanceof Integer[]);");
	}

	@Test
	public void testChars() {
		assertSuccess("char c = 'a'; assert c == 'a';");
		assertSuccess("char c = 'a' + 1; assert c == 'b';");
		assertSuccess("char c = 'b' - 1; assert c == 'a';");
		assertSuccess("char c = 'a' + (byte)1; assert c == 'b';");
		assertSuccess("char c = 'a' + (short)1; assert c == 'b';");
		assertSuccess("char c = 'a' + (int)1; assert c == 'b';");

		assertSuccess("char c = 1; assert c == 1;");
		assertSuccess("char c = (byte)1; assert c == 1;");
		assertSuccess("char c = (short)1; assert c == 1;");
		assertSuccess("char c = (int)1; assert c == 1;");
		assertSuccess("char c = (int)1; assert c == 1;");
		assertSuccess("char c = (char)-1; assert c == 65535;");
		assertSuccess("int c = (char)-1; assert c == 65535;");

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

		assertSuccess("char c = \"x\".charAt(0); assert c == 'x';");
		assertSuccess("String s = \"[\" + 'a' + 'b' + 'c' + \"]\"; assert s.equals(\"[abc]\");");

		assertSuccess("int c = 'a'; c++; assert c == 'b';");
		assertSuccess("int c = 'a'; ++c; assert c == 'b';");
		assertSuccess("int c = 'b'; c--; assert c == 'a';");
		assertSuccess("int c = 'b'; --c; assert c == 'a';");

		assertSuccess("String s = \"\"; for (char c = '1'; c <='9'; c++) {s += c;}; System.println(\"s=\" + s); assert s.equals(\"123456789\");");
	}

	@Test
	public void testLogicalSwitch() {
		assertSuccess("assert 1>0 ? true : false;");
		assertSuccess("assert 1>0 ? true : false;");
		assertSuccess("assert 1+1*2/1>2-2 ? 1/1>0*2 : 0*2>1/1;");
		assertSuccess("assert 1>0?1>0?true:false:false;");
		assertSuccess("assert 1>0?1<0?false:true:false;");
		assertSuccess("assert 1<0?1<0?false:false:true;");
		assertSuccess("assert 1>0?1>0?1>0?true:false:false:false;");
		assertSuccess("assert 1>0?1<0?1>0?false:false:true:false;");
		assertSuccess("assert 1>0?1>0?1<0?false:true:false:false;");
		assertSuccess("assert 1<0?1>0?1>0?false:false:false:true;");
		assertSuccess("assert 1>0?1>0?1>0?1>0?true:false:false:false:false;");

		assertFailCompile("Long x = true ? \"\" : new Integer(1);", // compiler set value ""
				"incompatible types: String cannot be converted to Long");
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
		assertSuccess("int x = 1; assert x++ + x++ == 3;");
		assertSuccess("int x = 1; assert ++x + ++x == 5;");
		assertSuccess("int x = 1; int y = ++x + x++ + x; assert x == 3; assert y == 7;");

		assertSuccess("int x = -1; assert x-- + x-- == -3;");
		assertSuccess("int x = -1; assert --x + --x == -5;");
		assertSuccess("int x = -1; int y = --x + x-- + x; assert x == -3; assert y == -7;");

		for (String t : new String[] {"byte", "short", "char", "int", "long", "float", "double"}) {
			assertSuccess(t + " x = 1; x++; assert x == 2;");
			assertSuccess(t + " x = 1; x--; assert x == 0;");
			assertSuccess(t + " x = 1; ++x; assert x == 2;");
			assertSuccess(t + " x = 1; --x; assert x == 0;");
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
		assertSuccess("boolean x = !true; assert !x;");
		assertSuccess("boolean x = !!true; assert x;");
		assertSuccess("boolean x = !! !! !! !! !! true; assert x;");
		assertSuccess("boolean x = !false; assert x;");
		assertSuccess("boolean x = true; assert !x == false; assert !!x == true; assert !!!x == false; assert !!!!x == true; assert !!!!!x == false;");
	}

	@Test
	public void testObject() {
		assertSuccess("Object o1 = new Object(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
		assertSuccess("class O{} O o1 = new O(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
		assertSuccess("class O extends Object{} O o1 = new O(); Object o2 = o1; O o3 = (O)o2;");
		assertSuccess("class O{int x = 0; public boolean equals(Object o){return x == ((O)o).x;}} O o1 = new O(); O o2 = new O(); assert o1 != o2; assert o1.equals(o2); o2.x++; assert !o1.equals(o2);");
		assertSuccess("assert new Object(){int m(int x){return x;}}.m(123) == 123;");
		assertSuccess("var o = new Object(){void m(){}}; o.m();");

		assertFailCompile("class O{} O o1 = new O(); Object o2 = o1; O o3 = o2;", //
				"incompatible types: Object cannot be converted to O");
		assertFailCompile("Object o = new Object(){void m(){}}; o.m();", //
				"cannot resolve method 'm' in 'Object'");
	}

	@Test
	public void variableWithoutName() {
		assertSuccess("String[] arr = {\"a\", \"b\", \"c\"}; int i = 0; for(String _ : arr) i++; assert i == arr.length;");
	}

	@Test
	public void testSwitch() {
		assertSuccess("int x = 1; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 10;");
		assertSuccess("int x = 2; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 20;");
		assertSuccess("int x = 3; int y = switch(x){case 1 -> 10; case 2 -> 20; default -> 30;}; assert y == 30;");

		assertSuccess("String x = null; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 0;");
		assertSuccess("String x = \"a\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 1;");
		assertSuccess("String x = \"b\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 1;");
		assertSuccess("String x = \"c\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; default -> 2;}; assert y == 2;");

		assertSuccess("int x = switch(\"a\"){case String s when s.length() == 1 -> 1; case String s when s.length() == 0 -> 2;}; assert x == 1;");
		assertSuccess("class O{int x; O(int x){this.x = x;}} int x = switch(new O(2)){case O o when o.x == 1 -> 1; case O o when o.x == 2 -> o.x + 1;} assert x == 3;");

		// TODO not all cases
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
			assertSuccess(t + " x = (" + t + ")127.0; assert x == 127;");
			for (String t2 : types) {
				assertSuccess(t + " x = (" + t + ")(" + t2 + ")127; assert x == 127;");
			}
			assertSuccess(t + " x = (" + t + ")(double)(float)(char)(long)(byte)127.0; assert x == 127;");
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

			assertSuccess(t + "[] x = (" + t + "[])new " + t + "[]{127}; assert x[0] == 127;");
			for (String t2 : new String[] {"byte", "short", "int", "long", "float", "double", "char"}) {
				if (!t.equals(t2)) {
					assertFailCompile(t + "[] x = (" + t2 + "[])new " + t + "[]{127}; assert x[0] == 127;", //
							"inconvertible types; cannot cast '" + t + "[]' to '" + t2 + "[]'");
					assertFailCompile(t + "[] x = (" + t + "[])new " + t2 + "[]{127}; assert x[0] == 127;", //
							"inconvertible types; cannot cast '" + t2 + "[]' to '" + t + "[]'");
				}
			}

			assertSuccess("Number a = (" + t + ")1; " + t + " x = (" + t + ")a; assert x == 1;");
		}
		assertSuccess("String s = (String)(\"a=\" + 1); assert s == \"a=1\";");

		// autocast
		assertSuccess("byte x = 1; assert x == (byte)1;");
		assertSuccess("byte x = 'a'; assert x == (byte)'a';");
		assertFailCompile("byte x = 1l;", //
				"incompatible types: long cannot be converted to byte");
		assertFailCompile("byte x = 1f;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("byte x = 1d;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte x = 129;", //
				"incompatible types: int cannot be converted to byte");

		assertSuccess("short x = 1; assert x == (short)1;");
		assertSuccess("short x = 'a'; assert x == (short)'a';");
		assertFailCompile("short x = 1l;", //
				"incompatible types: long cannot be converted to short");
		assertFailCompile("short x = 1f;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("short x = 1d;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short x = " + (Short.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to short");

		assertSuccess("int x = 1; assert x == (int)1;");
		assertSuccess("int x = 'a'; assert x == (int)'a';");
		assertFailCompile("int x = 1l;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("int x = 1f;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int x = 1d;", //
				"incompatible types: double cannot be converted to int");

		assertSuccess("long x = 1; assert x == 1L;");
		assertSuccess("long x = 'a'; assert x == (long)'a';");
		assertFailCompile("long x = 1f;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long x = 1d;", //
				"incompatible types: double cannot be converted to long");

		assertSuccess("float x = 1; assert x == 1f;");
		assertSuccess("float x = 'a'; assert x == (float)'a';");
		assertFailCompile("float x = 1d;", //
				"incompatible types: double cannot be converted to float");

		assertSuccess("double x = 1; assert x == 1d;");
		assertSuccess("double x = 'a'; assert x == (double)'a';");
		assertSuccess("double x = 1L; assert x == 1d;");
		assertSuccess("double x = 1f; assert x == 1d;");

		// assignments
		assertSuccess("byte a = 1; byte x = a; assert x == 1;");
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

		assertSuccess("byte a = 1; short x = a; assert x == 1;");
		assertSuccess("short a = 1; short x = a; assert x == 1;");
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
		assertSuccess("char a = 1; char x = a; assert x == 1;");
		assertFailCompile("int a = 1; char x = a; assert x == 1;", //
				"incompatible types: int cannot be converted to char");
		assertFailCompile("long a = 1; char x = a; assert x == 1;", //
				"incompatible types: long cannot be converted to char");
		assertFailCompile("float a = 1; char x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to char");
		assertFailCompile("double a = 1; char x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to char");

		assertSuccess("byte a = 1; int x = a; assert x == 1;");
		assertSuccess("short a = 1; int x = a; assert x == 1;");
		assertSuccess("char a = 1; int x = a; assert x == 1;");
		assertSuccess("int a = 1; int x = a; assert x == 1;");
		assertFailCompile("long a = 1; int x = a;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("float a = 1; int x = a; assert x == 1;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("double a = 1; int x = a; assert x == 1;", //
				"incompatible types: double cannot be converted to int");

		assertSuccess("byte a = 1; long x = a; assert x == 1L;");
		assertSuccess("short a = 1; long x = a; assert x == 1L;");
		assertSuccess("char a = 1; long x = a; assert x == 1L;");
		assertSuccess("int a = 1; long x = a; assert x == 1L;");
		assertSuccess("long a = 1; long x = a; assert x == 1L;");
		assertFailCompile("float a = 1; long x = a; assert x == 1L;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("double a = 1; long x = a; assert x == 1L;", //
				"incompatible types: double cannot be converted to long");

		assertSuccess("byte a = 1; float x = a; assert x == 1F;");
		assertSuccess("short a = 1; float x = a; assert x == 1F;");
		assertSuccess("char a = 1; float x = a; assert x == 1F;");
		assertSuccess("int a = 1; float x = a; assert x == 1F;");
		assertSuccess("long a = 1; float x = a; assert x == 1F;");
		assertSuccess("float a = 1; float x = a; assert x == 1F;");
		assertFailCompile("double a = 1; float x = a; assert x == 1F;", //
				"incompatible types: double cannot be converted to float");

		assertSuccess("byte a = 1; double x = a; assert x == 1D;");
		assertSuccess("short a = 1; double x = a; assert x == 1D;");
		assertSuccess("char a = 1; double x = a; assert x == 1D;");
		assertSuccess("int a = 1; double x = a; assert x == 1D;");
		assertSuccess("long a = 1; double x = a; assert x == 1D;");
		assertSuccess("float a = 1; double x = a; assert x == 1D;");
		assertSuccess("double a = 1; double x = a; assert x == 1D;");

		assertFail("Object a = 1; String s = (String)a;", //
				"cannot cast Integer to String");

		// Number
		assertFailCompile("Number x = 1; boolean y = (boolean)x;", //
				"cannot cast Number to boolean");

		// nulls
		assertSuccess("Byte x = (Byte)null;");
		assertSuccess("Short x = (Short)null;");
		assertSuccess("Character x = (Character)null;");
		assertSuccess("Integer x = (Integer)null;");
		assertSuccess("Long x = (Long)null;");
		assertSuccess("Float x = (Float)null;");
		assertSuccess("Double x = (Double)null;");
		assertSuccess("String x = (String)null;");
		assertSuccess("class A{} A x = (A)null;");
	}

	@Test
	public void testNotEquals() {
		assertSuccess("assert 1 != 2;");
		assertSuccess("assert 1.0 != 2.0;");
		assertSuccess("assert 1f != 2.0;");
		assertSuccess("assert 1 != 1.1f;");
		assertSuccess("assert true != false;");

		assertSuccess("assert \"a\" != \"b\";");
		assertSuccess("assert new Integer(1) != new Integer(2);");

		assertSuccess("var x = 1; var y = 2; assert x != y;");
		assertSuccess("var x = 1; int y = 2; assert x != y;");

		assertFailCompile("assert true != 2;", //
				"operator '!=' can not be applied to boolean, int");
		assertFail("assert \"a\" != \"a\";", //
				"Assert failed");
		assertSuccess("assert new Integer(1) != new Integer(1);");

		// equals
		assertSuccess("assert \"a\" == \"a\";");
		assertFail("assert \"a\" == \"b\";", //
				"Assert failed");
	}

	@Test
	public void testArrays() {
		assertSuccess("int[] x = {2}; x[0] *= 2; assert x[0] == 4;");
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
	}

	@Test
	public void testGetAndSet() {
		for (String t : new String[] {"byte", "short", "int", "long", "float", "double"}) {
			assertSuccess("byte x = 1; " + t + " y = x;");
		}
		for (String t : new String[] {"short", "int", "long", "float", "double"}) {
			assertSuccess("short x = 1; " + t + " y = x;");
		}
		for (String t : new String[] {"int", "long", "float", "double"}) {
			assertSuccess("int x = 1; " + t + " y = x;");
		}
	}

	@Test
	public void testUnnamedVariable() {
		// lambda
		assertSuccess("interface A{int m(int x);} A a = (_) -> 1; assert a.m(0) == 1;");
		assertSuccess("interface A{int m(int x);} A a = _ -> 1; assert a.m(0) == 1;");
		assertSuccess("interface A{int m(int x, int y);} A a = (_, _) -> 1; assert a.m(0, 0) == 1;");
		assertFailCompile("interface A{int m(int x, int y);} A a = (_, _) -> _;", //
				"unnamed variable cannot be used in expressions");

		// class
		assertFailCompile("class _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{void _(){}};", //
				"keyword '_' cannot be used as an identifier");

		// interface
		assertFailCompile("interface _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("interface A{void _();};", //
				"keyword '_' cannot be used as an identifier");

		// annotation
		assertFailCompile("@interface _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("@interface A{int _();};", //
				"keyword '_' cannot be used as an identifier");

		// enum
		assertFailCompile("enum _{a, b, c};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("enum E{_};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("enum E{_, _};", //
				"keyword '_' cannot be used as an identifier");

		// record
		assertFailCompile("record _(int x);", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("record R(int _);", //
				"keyword '_' cannot be used as an identifier");

		// field
		assertFailCompile("int _;", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("int _ = 1;", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{int _;};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{int _ = 1;};", //
				"keyword '_' cannot be used as an identifier");

		// method
		assertFailCompile("_(1);", //
				"keyword '_' cannot be used as an identifier", //
				"cannot resolve method '_'");

		// instanceof
		assertSuccess("class A{int x = 1;} Object o = new A(); if(o instanceof A _) {}");
		assertSuccess("record R(int x, int y); Object o = new R(1, 2); if(o instanceof R _) {}");
		assertSuccess("record R(int x, int y); Object o = new R(1, 2); if(o instanceof R(int _, int YYY) r && YYY == 2) {assert YYY == 2; YYY = 3; assert r.getY() == 3;}");
		assertSuccess("record R(int x); Object o = new R(1); if(o instanceof R(int _) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccess("record R(int x); Object o = new R(1); if(o instanceof R(var _) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccess("record R(int x); Object o = new R(1); if(o instanceof R(_) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccess("record R(int x, int y, int z); Object o = new R(1, 2, 3); if(o instanceof R(_, y, _)) {assert y == 2;}");

		// try-catch
		assertSuccess("try{} catch(Exception _){}");
		assertSuccess("class E1 extends Exception{} class E2 extends Exception{} try{} catch(E1 | E2 _){}");
		assertSuccess("class E1 extends Exception{} class E2 extends Exception{} try{} catch(E1 _) {} catch(E2 _){}");
		assertSuccess("AutoCloseable resource = ()->{}; try(var _ = resource){}");
		assertSuccess("AutoCloseable r1 = ()->{}; AutoCloseable r2 = ()->{}; try(var _ = r1; var _ = r2){}");
		assertFailCompile("try{} catch(Exception _){_.printStackTrace();}", //
				"unnamed variable cannot be used in expressions");

		// for
		assertSuccess("var list = new ArrayList(); list.add(\"a\"); for(Object _ : list){}");
		assertSuccess("var list = new ArrayList(); list.add(\"a\"); for(var _ : list){}");
		assertFailCompile("for(int _ = 0; _ < 1; _++) {}", //
				"keyword '_' cannot be used as an identifier");

		// labels
		assertFailCompile("_: {break _;}", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("while(true) _: {continue _;}", //
				"keyword '_' cannot be used as an identifier");

		// switch
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(int x, int _) r when x == 1 && r.getY() == 2: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(int _, int _) r when r.getX() == 1 && r.getY() == 2: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(var _, var _) r when r.getX() == 1 && r.getY() == 2: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(_, _) r when r.getX() == 1 && r.getY() == 2: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(x, y) when x == 1 && y == 2: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(x, _) _ when x == 1: return;} assert false;");
		assertSuccess("record R(int x, int y); switch(new R(1, 2)){case R(int _, int _) r when r.getX() == 1: return;} assert false;");

		//switch expression
		// TODO
	}
}
