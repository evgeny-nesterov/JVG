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

        assertFailCompile("int a = 1 + true;");
        assertFailCompile("int a = true | 1;");
        assertFailCompile("int a = true;");
        assertFailCompile("int a = !0;");
        assertFailCompile("boolean a = 1.0;");
        assertFailCompile("boolean a = +true;");
        assertFailCompile("boolean a = -true;");
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
                for (String o : new String[]{"+", "-", "*", "/", "%", ">", ">=", "<", "<=", "==", "!="}) {
                    // Integer x1=63; byte x2=3; assert (x1 + x2) == ((int)63 + (byte)3);
                    assertSuccessSerialize(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

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
                for (String o : new String[]{"&", "|", "^", "<<", ">>", ">>>"}) {
                    // Integer x1=63; byte x2=3; assert (x1 + x2) == ((int)63 + (byte)3);
                    assertSuccessSerialize(bt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(bt1 + " x1=63; " + pt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(pt1 + " x1=63; " + bt2 + " x2=3; assert (x1 " + o + " x2) == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");

                    // assert (new Integer((int)63) + (byte)3) == (63 + 3);
                    assertSuccessSerialize("assert (new " + bt1 + "((" + pt1 + ")63) " + o + " (" + pt2 + ")3) == (63 " + o + " 3);");
                    assertSuccessSerialize("assert ((" + pt2 + ")63 " + o + " new " + bt1 + "((" + pt1 + ")3)) == (63 " + o + " 3);");
                }
            }
        }

        // test +=, -= ...
        String[] boxIntTypes2 = {"Integer", "Long", "Character"};
        String[] priIntTypes2 = new String[]{"int", "long", "char"};
        for (int i = 0; i < boxIntTypes2.length; i++) {
            String bt1 = boxIntTypes2[i];
            String pt1 = priIntTypes2[i];
            for (int j = 0; j < boxIntTypes1.length; j++) {
                String bt2 = boxIntTypes1[j];
                String pt2 = priIntTypes1[j];
                for (String o : new String[]{"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
                    assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")63; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")63); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")63 " + o + " (" + pt2 + ")3);");
                }
            }
        }
        String[] boxIntTypes3 = {"Byte", "Short"};
        String[] priIntTypes3 = new String[]{"byte", "short"};
        for (int i = 0; i < boxIntTypes3.length; i++) {
            String bt1 = boxIntTypes3[i];
            String pt1 = priIntTypes3[i];
            for (int j = 0; j < boxIntTypes1.length; j++) {
                String bt2 = boxIntTypes1[j];
                String pt2 = priIntTypes1[j];
                for (String o : new String[]{"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
                    assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                }
            }
        }

        String[] boxIntTypesAll = {"Byte", "Short", "Integer", "Long", "Float", "Double", "Character"};
        String[] priIntTypesALl = {"byte", "short", "int", "long", "float", "double", "char"};
        String[] operations = new String[]{"+", "-", "*", "/", "%"};
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

                    if (bt1.equals("Byte") || bt1.equals("Short")) {
                        assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    } else {
                        assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= (" + pt2 + ")3; assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    }
                    assertSuccessSerialize(pt1 + " x1 = (" + pt1 + ")15; x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    if (bt1.equals("Byte") || bt1.equals("Short")) {
                        assertFailCompile(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    } else {
                        assertSuccessSerialize(bt1 + " x1 = new " + bt1 + "((" + pt1 + ")15); x1 " + o + "= new " + bt2 + "((" + pt2 + ")3); assert x1 == ((" + pt1 + ")15 " + o + " (" + pt2 + ")3);");
                    }
                }
            }
        }

        for (String o : new String[]{"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"}) {
            assertFailCompile("1" + o + "=1;");
            assertFailCompile("this" + o + "=1;");
            assertFailCompile("class A{} A" + o + "=1;");
            assertFailCompile("class A{} new A()" + o + "=1;");
            assertFailCompile("class A{int m(){return 1;}} new A().m()" + o + "=1;");
        }
        assertFailCompile("1++;");
        assertFailCompile("1--;");
        assertFailCompile("++1;");
        assertFailCompile("--1;");
        assertFailCompile("boolean x = !1;");
        assertFailCompile("boolean x = true; x++;");
        assertFailCompile("boolean x = true; x--+;");
        assertFailCompile("boolean x = true; ++x;");
        assertFailCompile("boolean x = true; --x;");
        assertFailCompile("String x = \"\"; x++;");
        assertFailCompile("String x = \"\"; x--+;");
        assertFailCompile("String x = \"\"; ++x;");
        assertFailCompile("String x = \"\"; --x;");

        for (int i = 0; i < priIntTypesALl.length; i++) {
            String pt = priIntTypesALl[i];
            for (String o : operations) {
                assertFailCompile("var x = (" + pt + ")0 " + o + " new Object();");
                assertFailCompile("var x = new Object() " + o + " (" + pt + ")0;");
                assertFailCompile("var x = (" + pt + ")0 " + o + " true;");
                assertFailCompile("var x = false " + o + " (" + pt + ")0;");
                assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = x" + o + " new Object();");
                assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = new Object() " + o + " x;");
                assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = x" + o + " true;");
                assertFailCompile(pt + " x = (" + pt + ")0; " + pt + " y = true" + o + " x;");
            }
        }

        // check operations with vars
        for (String t : new String[]{"byte", "short", "int", "long", "float", "double", "char", "Byte", "Short", "Integer", "Long", "Float", "Double", "Character"}) {
            for (String o : new String[]{"+", "-", "*", "/", "%"}) {
                assertSuccessSerialize("interface I{double get(" + t + " x, " + t + " y);} I o = (x, y) -> x " + o + " y;");
            }
        }
        assertSuccessSerialize("interface I{String get(String x, String y);} I o = (x, y) -> x + y;");
        for (String o : new String[]{"|", "||", "&", "&&", "^"}) {
            assertSuccessSerialize("interface I{boolean get(boolean x, boolean y);} I o = (x, y) -> x " + o + " y;");
        }
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
    }

    @Test
    public void testNumbersCasts() {
        String[] prefixes1 = {" ", "(byte)", "(short)", "(int)", "(long)", "(double)", "(float)", "(char)"};
        String[] prefixes2 = {"+", "- -", "+ +", " "};
        String[] suffixes = {"", ".", ".0", ".0000", "f", "F", "d", "D", "l", "L", ".d", ".D", ".0d", ".0D", "e+0", "e-0", "0e-1"};
        testSuccessNumbersCasts(prefixes1, prefixes2, suffixes, new String[]{"1", "127"});

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

        assertFailCompile("char c1 = -1;");
        assertFailCompile("char c1 = 'a'; char c2 = c1 + '1';");
        assertFailCompile("char c1 = 'a'; char c2 = c1 + 1;");
        assertFailCompile("char c1 = 'a'; char c2 = c1 + 1l;");
        assertFailCompile("char c1 = 'a'; char c2 = c1 + (byte)1;");
        assertFailCompile("char c1 = 'a'; char c2 = c1 + (short)1;");

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

        assertFailCompile("Long x = true ? \"\" : new Integer(1);");
        assertFailCompile("int x = true ? 1 : \"\";");
        assertFailCompile("int x = 1 ? 1 : 2;");
        assertFailCompile("int x = 1 ? true : 2;");
        assertFailCompile("int x = true ? ;");
        assertFailCompile("int x = true ? 1;");
        assertFailCompile("int x = true ? 1 : ;");
        assertFailCompile("int x = true ? : 1;");
        assertFailCompile("int x = true ? : ;");
    }

    @Test
    public void testIncrements() {
        assertSuccessSerialize("int x = 1; assert x++ + x++ == 3;");
        assertSuccessSerialize("int x = 1; assert ++x + ++x == 5;");
        assertSuccessSerialize("int x = 1; int y = ++x + x++ + x; assert x == 3; assert y == 7;");

        assertSuccessSerialize("int x = -1; assert x-- + x-- == -3;");
        assertSuccessSerialize("int x = -1; assert --x + --x == -5;");
        assertSuccessSerialize("int x = -1; int y = --x + x-- + x; assert x == -3; assert y == -7;");

        for (String t : new String[]{"byte", "short", "char", "int", "long", "float", "double"}) {
            assertSuccessSerialize(t + " x = 1; x++; assert x == 2;");
            assertSuccessSerialize(t + " x = 1; x--; assert x == 0;");
            assertSuccessSerialize(t + " x = 1; ++x; assert x == 2;");
            assertSuccessSerialize(t + " x = 1; --x; assert x == 0;");
        }

        assertFailCompile("String s = \"\"; s++;");
        assertFailCompile("String s = \"\"; s--;");
        assertFailCompile("String s = \"\"; ++s;");
        assertFailCompile("String s = \"\"; --s;");
        assertFailCompile("boolean s = true; s++;");
        assertFailCompile("boolean s = true; s--;");
        assertFailCompile("boolean s = true; ++s;");
        assertFailCompile("boolean s = true; --s;");
    }

    @Test
    public void testBoolean() {
        assertSuccessSerialize("boolean x = !true; assert !x;");
        assertSuccessSerialize("boolean x = !false; assert x;");
        assertSuccessSerialize("boolean x = true; assert !x == false; assert !!x == true; assert !!!x == false; assert !!!!x == true; assert !!!!!x == false;");
    }

    @Test
    public void testObject() {
        assertSuccessSerialize("Object o1 = new Object(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
        assertSuccessSerialize("class O{} O o1 = new O(); Object o2 = o1; assert o1 == o2; assert o1.equals(o2);");
        assertSuccessSerialize("class O extends Object{} O o1 = new O(); Object o2 = o1; O o3 = (O)o2;");
        assertFailCompile("class O{} O o1 = new O(); Object o2 = o1; O o3 = o2;");
        assertSuccessSerialize("class O{int x = 0; public boolean equals(Object o){return x == ((O)o).x;}} O o1 = new O(); O o2 = new O(); assert o1 != o2; assert o1.equals(o2); o2.x++; assert !o1.equals(o2);");
        assertFailCompile("class O extends O{}");
        assertFailCompile("interface I extends I{}");
        assertSuccessSerialize("assert new Object(){int m(int x){return x;}}.m(123) == 123;");
        assertFailCompile("Object o = new Object(){void m(){}}; o.m();");
        assertSuccessSerialize("var o = new Object(){void m(){}}; o.m();");
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

        // TODO
        // assertFailCompile("int x = 2; int y = switch(x){case 1 -> 10; case 2 -> 20;};");
        // assertFailCompile("String x = \"c\"; int y = switch(x){case null -> 0; case \"a\", \"b\" -> 1; case \"c\" -> 2;};");
    }

    @Test
    public void testCast() {
        for (String t : new String[]{"byte", "short", "int", "long", "float", "double", "char"}) {
            assertSuccessSerialize(t + " x = (" + t + ")127.0; assert x == 127;");
            for (String t2 : new String[]{"byte", "short", "int", "long", "float", "double", "char"}) {
                assertSuccessSerialize(t + " x = (" + t + ")(" + t2 + ")127; assert x == 127;");
            }
            assertSuccessSerialize(t + " x = (" + t + ")(double)(char)(long)(byte)127; assert x == 127;");
            assertFailCompile(t + " x = (boolean)(byte)1;");
            assertFailCompile(t + " x = (" + t + ")\"1\";");
            assertFailCompile(t + " x = (" + t + ")true;");
            assertFailCompile(t + " x = (" + t + ");");
            assertFailCompile(t + " x = (" + t + ")new int[0];");
            assertFailCompile(t + " x = (" + t + ")new String[1];");
            assertFailCompile(t + " x = (void)1;");
            assertFailCompile("String s = (String)(" + t + ")1;");
            assertFailCompile("boolean b = (boolean)(" + t + ")1;");
            assertFailCompile("String s = (" + t + ")\"\";");
            assertFailCompile("void s = (" + t + ")1;");
            assertFailCompile("boolean b = (" + t + ")true;");
            assertFailCompile("class A{} " + t + " x = (" + t + ")A;");

            assertSuccessSerialize(t + "[] x = (" + t + "[])new " + t + "[]{127}; assert x[0] == 127;");
            for (String t2 : new String[]{"byte", "short", "int", "long", "float", "double", "char"}) {
                if (!t.equals(t2)) {
                    assertFailCompile(t + "[] x = (" + t2 + "[])new " + t + "[]{127}; assert x[0] == 127;");
                    assertFailCompile(t + "[] x = (" + t + "[])new " + t2 + "[]{127}; assert x[0] == 127;");
                }
            }
        }

        // autocast
        assertSuccessSerialize("byte x = 1; assert x == (byte)1;");
        assertSuccessSerialize("byte x = 'a'; assert x == (byte)'a';");
        assertFailCompile("byte x = 1l;");
        assertFailCompile("byte x = 1f;");
        assertFailCompile("byte x = 1d;");
        assertFailCompile("byte x = 129;");

        assertSuccessSerialize("short x = 1; assert x == (short)1;");
        assertSuccessSerialize("short x = 'a'; assert x == (short)'a';");
        assertFailCompile("short x = 1l;");
        assertFailCompile("short x = 1f;");
        assertFailCompile("short x = 1d;");
        assertFailCompile("short x = " + (Short.MAX_VALUE + 1) + ";");

        assertSuccessSerialize("int x = 1; assert x == (int)1;");
        assertSuccessSerialize("int x = 'a'; assert x == (int)'a';");
        assertFailCompile("int x = 1l;");
        assertFailCompile("int x = 1f;");
        assertFailCompile("int x = 1d;");

        assertSuccessSerialize("long x = 1; assert x == 1L;");
        assertSuccessSerialize("long x = 'a'; assert x == (long)'a';");
        assertFailCompile("long x = 1f;");
        assertFailCompile("long x = 1d;");

        assertSuccessSerialize("float x = 1; assert x == 1f;");
        assertSuccessSerialize("float x = 'a'; assert x == (float)'a';");
        assertFailCompile("float x = 1d;");

        assertSuccessSerialize("double x = 1; assert x == 1d;");
        assertSuccessSerialize("double x = 'a'; assert x == (double)'a';");
        assertSuccessSerialize("double x = 1L; assert x == 1d;");
        assertSuccessSerialize("double x = 1f; assert x == 1d;");
    }
}
