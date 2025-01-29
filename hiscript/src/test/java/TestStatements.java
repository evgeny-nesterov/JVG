import org.junit.jupiter.api.Test;

public class TestStatements extends HiTest {
    @Test
    public void testDeclaration() {
        assertSuccessSerialize("int a;");
        assertSuccessSerialize("int a123;");
        assertSuccessSerialize("int _a;");
        assertSuccessSerialize("int _;");
        assertSuccessSerialize("int _0123;");
        assertSuccessSerialize("int _a;");
        assertSuccessSerialize("int $;");
        assertSuccessSerialize("int _$;");
        assertSuccessSerialize("int $a__;");
        assertSuccessSerialize("int ____$a_$b_$c___;");
        assertSuccessSerialize("int переменная;");

        assertSuccessSerialize("int a = 1; assert a == 1;");
        assertSuccessSerialize("String a, b, c;");
        assertSuccessSerialize("String a = \"a\", b, c = null; assert \"a\".equals(a); assert c == null;");
        assertSuccessSerialize("{int a = 1;} int a = 2; assert a == 2;");

        assertFailCompile("String 0var;");
        assertFailCompile("String a, a;");
        assertFailCompile("int a = 1; String a = \"\";");
        assertFailCompile("int x; int y = x;");
        assertFailCompile("int x = 1; int y = true || x;");
        assertFailCompile("int x = 1, y = 0.0;");
    }

    @Test
    public void testFor() {
        assertSuccessSerialize("int j = 0; for(int i = 0; i < 10; i++) {assert i == j; j++;}");
        assertSuccessSerialize("int i = 0; for(; i < 10; i++); assert i == 10;");
        assertSuccessSerialize("int x = 0; for(int i = 0, j = 10; i < 10 && j >= 0; i++, j--) {x++;} assert x == 10;");

        // iterable for
        assertSuccessSerialize("int x[] = {0, 1, 2, 3}; for (int i : x) {i++;}; for (int i : x); for (int i : x) break; for (int i : x) {continue;}");
        assertSuccessSerialize("String[] x = {\"a\", \"b\", \"c\"}; for (String i : x) {i += i;};");
        assertSuccessSerialize("int[] arr = {1, 2, 3}; int i = 0; for (int x : arr) assert x == arr[i++];");
        assertSuccessSerialize("ArrayList<String> l = new ArrayList<>(); l.add(\"a\"); l.add(\"b\"); l.add(\"c\"); int i = 0; for (String s : l) {assert s.equals(l.get(i++));}");

        assertSuccessSerialize("for(;;) {break;}");
        assertSuccessSerialize("for(int i = 0;;) {break;}");
        assertSuccessSerialize("int i = 0; for(;i < 0;); assert i == 0;");
        assertSuccessSerialize("int i = 0; for(;;i++, i++) {break;} assert i == 0;");
        assertSuccessSerialize("int i = 0; for(;;i++, i++) {{{break;}}} assert i == 0;");
        assertSuccessSerialize("for(long x : new int[]{1, 2, 3}){}");
        assertSuccessSerialize("int x = 0; for(int y : new int[]{1, 2, 3}){x=y; break;} assert x == 1;");
        assertSuccessSerialize("ArrayList<String> l = new ArrayList<>(); l.add(\"a\"); for(String x : l){assert x.equals(\"a\");}");
        assertSuccessSerialize("for(int x : new ArrayList<Integer>()){}");
        assertSuccessSerialize("for(Object x : new ArrayList()){}");

        assertFailCompile("for(private int x = 0; x < 10; x++){}");
        assertFailCompile("for(private int x : new long[]{1}){}");
        assertFailCompile("for(int x : new String[]{\"a\", \"b\"}){}");
        assertFailCompile("for(int x : new long[]{1L, 2L, 3L}){}");
        assertFailCompile("for(int x : new ArrayList()){}");
        assertFailCompile("for(int x : new ArrayList<String>()){}");
        assertFailCompile("for(Boolean x : new ArrayList<String>()){}");

        assertFailCompile("for() {}");
        assertFailCompile("for(;) {}");
        assertFailCompile("for(;;;) {}");
        assertFailCompile("for(;;) {");
        assertFailCompile("for(;;)");
        assertFailCompile("int x = 0; for(;x=1, x=2;) {}");
        assertFailCompile("for(int x : new Object()) {}");
    }

    @Test
    public void testWhile() {
        assertSuccessSerialize("int x = 3; while(x != 0) {x--;}");
        assertSuccessSerialize("int x = 3; while(true) {x--; if(x <= 1) break; else continue; x = 1;} assert x == 1;");

        assertFailCompile("while() {}");
        assertFailCompile("while(1) {}");
        assertFailCompile("while(1.1) {}");
        assertFailCompile("while(\"\") {}");
        assertFailCompile("while(true;) {}");
        assertFailCompile("while(true)");
        assertFailCompile("while(true) {");
    }

    @Test
    public void testDoWhile() {
        assertSuccessSerialize("int x = 3; do{x--;} while(x != 0); assert x == 0;");
        assertSuccessSerialize("int x = 3; do {x--;} while(x != 0); assert x == 0;");
        assertSuccessSerialize("int x = 3; do {x--; if(x == 1) break;} while(x != 0); assert x == 1;");
        assertSuccessSerialize("int x = 3; do {x--; if(x >= 1) continue; break;} while(true); assert x == 0;");

        assertFailCompile("do {} do(true);");
        assertFailCompile("do {} while();");
        assertFailCompile("do {} while(true;);");
    }

    @Test
    public void testIf() {
        assertSuccessSerialize("int x = 0; if (x == 0) x++; assert x == 1;");
        assertSuccessSerialize("if (false) assert false;");
        assertSuccessSerialize("if (true);");
        assertSuccessSerialize("int x = 0; if (x != 0) {x++;} assert x == 0;");
        assertSuccessSerialize("int x = 0; if (x != 0) {x = -1;} else {x = 1;} assert x == 1;");
        assertSuccessSerialize("int x = 2; if (x == 0) {assert false;} else if(x == 1) {assert false;} else x++; assert x == 3;");
        assertSuccessSerialize("int x = 1; if (x > 0) if(x != 0) if (x == 1) x++; assert x == 2;");
        assertSuccessSerialize("String s = \"a\"; if (s != null && s.length() == 1 && s.equals(\"a\")) {s = null;} assert s == null;");

        assertFailCompile("if() {}");
        assertFailCompile("if(true;) {}");
        assertFailCompile("if(true)");
        assertFailCompile("if(1) {}");
        assertFailCompile("if(1.0) {}");
        assertFailCompile("if(\"\") {}");
        assertFailCompile("if(true) {} else if(){}");
        assertFailCompile("if(true) {} else if(false)");
        assertFailCompile("if(true) {} else if(false){");
        assertFailCompile("if(true) {} else if(false){} else ");
        assertFailCompile("if(true) {} else if(false){} else {");
    }

    @Test
    public void testSwitch() {
        assertSuccessSerialize("switch(1){}");
        assertSuccessSerialize("switch((byte)1){}");
        assertSuccessSerialize("switch((short)1){}");
        assertSuccessSerialize("switch((char)1){}");
        assertFailCompile("switch(1L){}");
        assertFailCompile("switch(1F){}");
        assertFailCompile("switch(1.0){}");
        assertFailCompile("switch(true){}");
        assertSuccessSerialize("switch(\"a\"){case \"a\": break; default: assert false;}");
        assertSuccessSerialize("switch(new Object()){}");
        assertSuccessSerialize("enum E{e} switch(E.e){}");

        assertSuccessSerialize("switch(1){case 1,2,3: break; case 4,5,6: assert false; break; default: assert false; break;}");
        assertSuccessSerialize("switch(\"a\"){case \"a\",\"b\",\"c\": break; case \"d\",\"e\",\"f\": break; default: break;}");

        assertSuccessSerialize("int x = 1; switch(x){case 1: assert true;break; case 2: assert false;break;}");
        assertSuccessSerialize("int x = 1; switch(x){case 2: assert false;break; default: assert true;break;}");
        assertSuccessSerialize("int x = 1; switch(x){case 2, x == 1: assert true;break; case 1, x == 2: assert false;break;}");
        assertSuccessSerialize("int x = 2; switch(x) {case 0: x=0; break; case 1: x--; break; case 2, 3, x + 1: x++; break;} assert x == 3;");
        assertSuccessSerialize("int x = 1; switch(x) {case 0: x=0; case 1: x++; case 2: x++;} assert x == 3;");
        assertSuccessSerialize("int x = 4; switch(x) {case 0: x=0; break; case 1: x--; break; case 2: x++; break; default: x += 2;} assert x == 6;");
        assertSuccessSerialize("String s = \"c\"; switch(s) {case \"a\", \"x\": s = \"\"; break; case \"b\": break; case \"c\": s += 1; break;} assert s.equals(\"c1\");");
        assertSuccessSerialize("class A {}; class B{}; A a = new A(); Object b = new B(); switch(b) {case a: break; case new A(): break; case b: b = a;} assert b == a && b.equals(a);");
        assertSuccessSerialize("class O{int a; O(int a){this.a=a;}} switch(new O(1)){case O o when o.a == 0: assert false : \"case 1\"; case O o when o.a == 1: assert o.a == 1; return;} assert false : \"case 2\";");
        assertFailCompile("int x = 1; switch(x){case 1:break; case \"\":break;}");
        assertSuccessSerialize("Object s = null; switch(s){case \"a\": assert true;break; case \"b\": assert false;break; case null: assert s == null;break;}");
        assertSuccessSerialize("enum E{a,b,c} E e = E.b; switch(e){case a: assert false; case b: break; case E.c: assert false;}");
        assertFailCompile("enum E{a,b,c} E e = E.b; switch(e){case a: break; case b: break; case c: break; case d: break;}"); // cannot resolve symbol 'd'

        // switch object class
		assertSuccess("Object o = \"a\"; switch(o){case String s: assert s.equals(\"a\"): \"o=\" + o; break; case Integer i: assert false: \"Integer\"; case Object o2: assert false: \"Object\"; default: assert false: \"default\";}");
		assertSuccess("Object o = \"a\"; switch(o){case String s1 when s1.length() == 1: assert s1.length() == 1; break; case String s2: assert false: \"length != 1\";}");
		assertFailCompile("switch(\"\"){case Integer i, String s: break;}"); // several types in one case
//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases

        // format failure
        assertFailCompile("switch(){}");
        assertFailCompile("switch(1);");
        assertFailCompile("switch(1;){}");
        assertFailCompile("switch(1,2){}");
        assertFailCompile("switch(1){case}");
        assertFailCompile("switch(1){case :}");
        assertFailCompile("switch(1){case 1,2,:break;}");
        assertFailCompile("switch(1){case 1: break}");
        assertFailCompile("switch(1){case 1: break; default}");
        assertFailCompile("switch(1){default;}");
        assertFailCompile("switch(void){}");
        assertFailCompile("switch(int){}");
        assertFailCompile("switch(int x = 1){}");

        // failures
        assertFailCompile("class A{} switch(A){}");
        assertFailCompile("class A{void m(){}} switch(new A().m()){}");
        assertFailMessage("class A{int get(){throw new RuntimeException(\"error\");}} switch(new A().get()){case 1: break;}", "error");
        assertFailCompile("switch(1){case 1,2,1: break;}");
        assertFailCompile("switch(12){case 1, 12, 2*5+2: break;}");
        assertFailCompile("switch(12){case 1, ((2 * 8 + 18/3)/2-1)/2, 5: break;}");
        assertFailCompile("switch(\"a\"){case \"a\",\"b\",\"a\": break;}");
        assertFailCompile("switch(\"a\"){case \"ab\",\"b\",\"a\"+\"b\": break;}");
        assertFailCompile("switch(\"a\"){case \"a\": break; case \"b\", \"a\": break;}");
        assertFailCompile("enum E{e1,e2,e3} switch(E.e1){case e1,e2,e3,e1: break;}");
    }

    @Test
    public void testExceptions() {
        assertSuccessSerialize("new Exception(); new Exception(\"error\"); new RuntimeException(); new RuntimeException(\"error\");");
        assertSuccessSerialize("class E extends Exception {E(){} E(String msg){super(msg);}}; E e1 = new E(); E e2 = new E(\"error\"); assert \"error\".equals(e2.getMessage()); assert e1 instanceof Exception;");

        // throw in root
        assertFailCompile("throw new Exception(\"error\");"); // unreported exception
        assertFailCompile("throw new Exception(1, 2, 3);");
        assertFailSerialize("throw new RuntimeException(\"error\");");
        assertFailCompile("throw new Object();");
        assertFailCompile("throw \"\";");
        assertFailCompile("throw 1;");
        assertFailCompile("throw true;");
        assertFailCompile("class E extends Exception{} throw new E();"); // unreported exception
        assertFailSerialize("class E extends RuntimeException{} throw new E();");
        assertFail("Exception exc = null; try {throw new RuntimeException(\"error\");} catch(Exception e) {exc = e;} finally {if (exc != null) throw exc;}");

        // throw in try
        assertSuccessSerialize("try{throw new Exception(\"error\");} catch(Exception e){assert e.getMessage().equals(\"error\");} ");
        assertSuccessSerialize("class E extends Exception{E(String message){super(message);}} try {throw new E(\"error\");} catch(E e) {assert e.getMessage().equals(\"error\");} ");
        assertFailCompile("class E extends Exception{} try{throw new Exception();} catch(E e) {}"); // unreported exception
        assertFailSerialize("class E extends RuntimeException{} try{throw new RuntimeException();} catch(E e) {}");
        assertSuccessSerialize("class E extends Exception{} try{throw new Exception();} catch(E e){assert false;} catch(RuntimeException e){assert false;} catch(Exception e){}");
        assertSuccessSerialize("class E extends Exception {E(String message){super(message);}} " + //
                "class A {void m(int x) throws E {if (x == 1) throw new E(\"error-\" + x);}}" + //
                "try {A a = new A(); a.m(1);} catch(E e) {assert e.getMessage().equals(\"error-1\");}");
        assertSuccessSerialize("class A implements AutoCloseable{int x = 1; public void close(){x--;}} A a_; try(A a = a_= new A()) {assert a.x==1;} finally{assert a_.x==0;} assert a_.x==0;");
        assertSuccessSerialize("interface I extends AutoCloseable{} class A implements I{public void close(){}} try(A a1 = new A(); A a2 = new A()) {}");
        assertFailCompile("class A implements AutoCloseable{public void close(){}} try(A a = new A();) {}");
        assertSuccessSerialize("class E1 extends Exception{E1(){} E1(String msg){super(msg);}} class E2 extends Exception{E2(){} E2(String msg){super(msg);}} try{throw new E2(\"error\");} catch(E1 | E2 e){assert e.getMessage().equals(\"error\");}");
        assertFailMessage("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"close error\");}} try(A a = new A()) {}", "close error");
        assertFailMessage("class A implements AutoCloseable{public void close(){}} try(A a = null) {}", "null pointer");

        // Exception has already been caught
        assertFailCompile("try{} catch(Exception e){} catch(Exception e){}"); // Exception 'java.lang.Exception' has already been caught
        assertFailCompile("try{} catch(Exception e){} catch(RuntimeException e){}"); // Exception 'java.lang.RuntimeException' has already been caught
        assertFailCompile("try{} catch(RuntimeException e){} catch(RuntimeException e){}");
        assertFailCompile("class E extends Exception{} try{} catch(E e){} catch(E e){}");
        assertFailCompile("class E extends Exception{} try{} catch(Exception e){} catch(E e){}");
        assertFailCompile("try{} catch(Exception | Exception e){}");
        assertFailCompile("try{} catch(Exception | RuntimeException e){}");
        assertFailCompile("try{} catch(RuntimeException | Exception e){}");

        // in constructor
        assertFailCompile("class A{A() {throw new Exception();}}");
        assertFailCompile("class A{A() throws RuntimeException {throw new Exception();}}");
        assertFailCompile("class A{A() throws Exception {throw new Exception(); super();}}");
        assertFailCompile("class A{A() throws String {}}");
        assertFail("class A{A(){throw new RuntimeException();}} new A();");
        assertFail("class A{A(){throw new RuntimeException();}} class B extends A{B(){super();}} new B();");

        // in method
        assertFailCompile("class A{void m() {throw new Exception();}}");
        assertFailCompile("class A{void m() throws RuntimeException {throw new Exception();}}");
        assertFailCompile("class A{void m() throws String {}}");
        assertFail("class A{void m(){throw new RuntimeException();}} new A().m();");

        // in initialization
        assertFailCompile("class A{{throw new Exception();}}");
        assertFail("class A{{throw new RuntimeException();}} new A();");

        assertFailCompile("class A{static{throw new Exception();}}");
        assertSuccessSerialize("class A{static{throw new RuntimeException();}}");
        assertFail("class A{static int x = 1; static{throw new RuntimeException();}} int x = A.x;");
        assertFailCompile("try(int x = 1) {}");
        assertFailCompile("try(Object x = new Object()) {}");
        assertFailCompile("try {} catch(String | Integer e){}");
        assertFailCompile("try {} catch(RuntimeException | Integer e){}");

        assertSuccessSerialize("class A{int m(){try {throw new RuntimeException(\"1\");} catch(Exception e) {} finally{return 1;}}} assert new A().m() == 1;");
        assertSuccessSerialize("class A{int m(){try {throw new RuntimeException(\"error 1\");} catch(Exception e) {throw new RuntimeException(\"error 2\");} finally{return 1;}}} assert new A().m() == 1;"); // cancel exception and return 1 in finally

        // invalid format
        assertFailCompile("throw Exception();");
        assertFailCompile("throw new;");
        assertFailCompile("throw new Exception;");
        assertFailCompile("try()");
        assertFailCompile("try(){}");
        assertFailCompile("try{} catch(Exception ){}");
        assertFailCompile("try{} catch(Exception | e){}");
        assertFailCompile("try{} catch(Exception e){} finally");
        assertFailCompile("try(Object x) {}");
        assertFailCompile("try(Object x = new Object()) {}");
        assertFailCompile("try(String x = \"abc\") {}");
        assertFailCompile("try{} catch(Object e){}");
        assertFailCompile("try{} catch(Integer | Long | Byte e){}");
    }

    @Test
    public void testNew1() {
    }

    @Test
    public void testNew() {
        assertSuccessSerialize("new Object().toString();");
        assertSuccessSerialize("assert new Object(){public String toString(){return \"a\";}}.toString().equals(\"a\");");
        assertSuccessSerialize("assert (new int[]{1})[0] == 1;");
        assertSuccessSerialize("class A{} A a = new A(); assert a instanceof A;");
        assertSuccessSerialize("class A{A(int x){}} new A(1);");
        assertSuccessSerialize("assert (new int[1])[0] == 0;");
        assertSuccessSerialize("assert (new int[]{1,2,3})[2] == 3;");
        assertSuccessSerialize("assert (new int[1][1])[0][0] == 0;");

        assertSuccessSerialize("assert new boolean[][]{{false,false},{false,true}}[(byte)1][(char)1];");
        assertSuccessSerialize("assert new byte[][]{{(byte)1,(short)2},{(char)3,4}}[(byte)1][(char)1] == 4;");
        assertSuccessSerialize("assert new short[][]{{(byte)1,(short)2},{(char)3,4}}[(short)1][1] == 4;");
        assertSuccessSerialize("assert new char[][]{{(byte)1,(short)2},{3,4}}[1][1] == 4;");
        assertSuccessSerialize("assert new int[][]{{(byte)1,(short)2},{(char)3,4}}[1][1] == 4;");
        assertSuccessSerialize("assert new long[][]{{(byte)1,(short)2},{(char)3,4,5L}}[1][1] == 4;");
        assertSuccessSerialize("assert new float[][]{{(byte)1,(short)2},{(char)3,4,5f,6L}}[1][1] == 4;");
        assertSuccessSerialize("assert new double[][]{{1d,2f},{3L,4,(byte)5,(short)6,(char)7}}[(byte)1][(short)1] == 4;");

        assertFailCompile("boolean[] x = new boolean[]{1};");
        assertFailCompile("byte[] x = new byte[]{true};");
        assertFailCompile("byte[] x = new byte[]{1L};");
        assertFailCompile("byte[] x = new byte[]{1D};");
        assertFailCompile("byte[] x = new byte[]{1F};");
        assertFailCompile("short[] x = new short[]{true};");
        assertFailCompile("short[] x = new short[]{1L};");
        assertFailCompile("short[] x = new short[]{1D};");
        assertFailCompile("short[] x = new short[]{1F};");
        assertFailCompile("char[] x = new char[]{true};");
        assertFailCompile("char[] x = new char[]{1L};");
        assertFailCompile("char[] x = new char[]{1D};");
        assertFailCompile("char[] x = new char[]{1F};");
        assertFailCompile("int[] x = new int[]{true};");
        assertFailCompile("int[] x = new int[]{1L};");
        assertFailCompile("int[] x = new int[]{1D};");
        assertFailCompile("int[] x = new int[]{1F};");
        assertFailCompile("long[] x = new long[]{true};");
        assertFailCompile("long[] x = new long[]{1D};");
        assertFailCompile("long[] x = new long[]{1F};");
        assertFailCompile("float[] x = new float[]{true};");
        assertFailCompile("float[] x = new float[]{1D};");
        assertFailCompile("double[] x = new double[]{true};");
    }

    @Test
    public void testBreakContinueLabel() {
        assertSuccessSerialize("A:{break A;};");
        assertSuccessSerialize("A:{{{break A;}}};");
        assertSuccessSerialize("A:break A;");
        assertSuccessSerialize("A:for(int i=0; i< 3; i++){continue A;};");
        assertFailCompile("A:{continue A;};");
        assertSuccessSerialize("A:for(int i=0; i< 3; i++){{{continue A;}}};");
        assertSuccessSerialize("A:for(int i=0; i< 3; i++) continue A;");
        assertFailCompile("A:continue A;");
        assertSuccessSerialize("A:B:C:D:{break B;}");
        assertFailCompile("break A;");
        assertFailCompile("A:{} break A;");
        assertFailCompile("A:{break B;}");
        assertFailCompile("A:{continue B;}");

        assertSuccessSerialize("A:{if(true) break A; assert false;};");
        assertSuccessSerialize("FOR: for(;;) {int a = 1; switch(a){case 0: break; case 1: break FOR;} assert false;}");
        assertSuccessSerialize("for (int i = 0; i < 10; i++) {if(i<10) continue; assert false;}");
        assertSuccessSerialize("BLOCK: for (int i = 0; i < 10; i++) {if(i<10) continue BLOCK; assert false;}");
    }

    @Test
    public void testReturn() {
        // methods
        assertSuccessSerialize("class A{int m(){return 1;}} assert new A().m() == 1;");
        assertSuccessSerialize("class A{String m(){return \"1\";}} assert new A().m().equals(\"1\");");
        assertSuccessSerialize("class A{int x; A set(int x){this.x = x; return this;} int get(){return this.x;}} assert new A().set(123).get() == 123;");
        assertSuccessSerialize("class A{int x; A set(int y){x = y; return this;} int get(){return this.x;}} assert new A().set(123).get() == 123;");
        assertFailCompile("class A{int m(){return \"\";}}");
        assertFailCompile("class A{int m(){return 1L;}}");
        assertFailCompile("class A{int m(){return;}}");
        assertFailCompile("class A{void m(){return; int x = 1;}}");
        assertFailCompile("class A{void m(){return; return;}}");
        assertFailCompile("class A{void m(){return A;}}");

        assertSuccessSerialize("class A{void m(){return;}} new A().m();");
        assertFailCompile("class A{void m(){return;}} Object x = new A().m();");
        assertFailCompile("class A{void m(){return 1;}}");

        assertSuccessSerialize("class A{int m(){ if(true) {{{return 1;}}} else return 2; }} assert new A().m() == 1;");
        assertSuccessSerialize("class A{int m(int x) {if(x == 3) return x; return 0;}} assert new A().m(3) == 3;");

        // constructors
        assertSuccessSerialize("class A{A(){return;}}");
        assertFailCompile("class A{A(){return \"\";}}");
        assertFailCompile("class A{A(){return; int x = 1;}}");
        assertFailCompile("class A{A(){return; return;}}");

        assertSuccessSerialize("class A{int x = 0; A(){if(true) return; x = 1;}} assert new A().x == 0;");

        // initializers
        assertSuccessSerialize("class A{{int x = 0; return;}}");
        assertSuccessSerialize("class A{static{int x = 0; return;}}");
        assertFailCompile("class A{{return \"\";}}");
        assertFailCompile("class A{{return; int x = 0;}}");
        assertFailCompile("class A{static{return; return;}}");
        assertFailCompile("class A{static{{return;} return;}}");

        assertSuccessSerialize("class A{int x = 0; {if(true) return; x = 1;}} assert new A().x == 0;");
        assertSuccessSerialize("class A{int m(){try {return 1;} catch(Exception e) {return 2;} finally {return 3;}}} assert new A().m() == 3;");
        assertSuccessSerialize("class A{int m(){try {throw new Exception();} catch(Exception e) {return 2;} finally {return 3;}}} assert new A().m() == 3;");
    }

    @Test
    public void testUnreachable() {
        // return and throw
        assertFailCompile("class C{int m(){{return 1;} return 2;}}");
        assertFailCompile("class C{int m(){do {return 1;} while(true); return 2;}}");
        assertFailCompile("class C{int m(){synchronized(this) {return 1;} return 2;}}");
        assertFailCompile("class C{int m(){BLOCK:{return 1;} return 2;}}");
        assertFailCompile("class C{int m(){if(false) while(true) {{return 1;} return 2;}}}");

        assertFailCompile("class C{int m(){{{return 1;} return 3;} return 4;}}");
        assertFailCompile("class C{void m() {{{{return;} return;} return;} return;}}");
        assertFailCompile("class C{void m() throws Exception {{{{return;} return;} throw new Exception();} return;}}");

        assertFailCompile("class C{void m(){BLOCK:{break BLOCK; return;} return;}}");
        assertSuccessSerialize("class C{void m(){BLOCK:{break BLOCK;} return;}}");
        assertFailCompile("class C{void m(){BLOCK:{continue BLOCK; return;} return;}}");

        // break and continue
        assertFailCompile("int x = 1; switch(x) {case 1: break; break;}");
        assertFailCompile("int x = 1; switch(x) {case 1: {break;} break;}");
        assertFailCompile("int x = 1; switch(x) {case 1: BLOCK:{break;} break;}");
        assertFailCompile("int x = 1; switch(x) {case 1: synchronized(this){break;} break;}");
        assertSuccessSerialize("int x = 1; switch(x) {case 1: do{break;}while(true); break;}");

        // switch
        assertSuccessSerialize("int x = 1; switch(x) {case 1: break; case 2: break;} return;");
        assertSuccessSerialize("int x = 1; switch(x) {case 1: return; case 2: return;} return;");
        assertSuccessSerialize("int x = 1; switch(x) {case 1: return; default: break;} return;");
        assertFailCompile("int x = 1; switch(x) {case 1: return; default: return;} return;");
        assertFailCompile("int x = 1; BLOCK:{switch(x) {case 1: break BLOCK; default: break BLOCK;} break BLOCK;}");
        assertFailCompile("WHILE: while(true) {switch(x) {case 1: continue WHILE; default: continue WHILE;} int y = 0;}");

        // if
        assertSuccessSerialize("if(true) {return;} return;");
        assertSuccessSerialize("if(true) {return;} else if(false) {return;} return;");
        assertSuccessSerialize("if(true) {return;} else if(false) {return;} else {} return;");
        assertFailCompile("if(true) {return;} else if(false) {return;} else {return;} return;");

        // try
        assertSuccessSerialize("try {return;} catch(Exception e){} return;");
        assertSuccessSerialize("try {} catch(Exception e){return;} return;");
        assertFailCompile("try {} catch(Exception e){} finally{return;} return;");
        assertFailCompile("try {return;} catch(Exception e){} finally{return;} return;");
        assertFailCompile("try {} catch(Exception e){return;} finally{return;} return;");
        assertFailCompile("try {return;} catch(Exception e){return;} finally{return;} return;");
    }

    @Test
    public void testThis() {
        assertSuccessSerialize("class A{{Object a = this; assert a instanceof A;}} new A();");
        assertSuccessSerialize("class A{void m(){Object a = A.this; assert a instanceof A;}} new A().m();");
        assertSuccessSerialize("class A{int x = 1; {this.x = 2;}} assert new A().x == 2;");
        assertSuccessSerialize("class A{int x = 1; {A.this.x = 2;}} assert new A().x == 2;");
        assertSuccessSerialize("class A{int x(){return 1;} int y(){return this.x() + 1;}} assert new A().y() == 2;");
        assertSuccessSerialize("class A{int x(){return 1;} int y(){return A.this.x() + 1;}} assert new A().y() == 2;");

        assertFailCompile("this();");
        assertFailCompile("this;");
        assertFailCompile("this = null;");
    }

    @Test
    public void testSuper() {
        assertSuccessSerialize("class A{int x = 1;} class B extends A{int x = 2; int get(){return super.x;}} assert new B().get() == 1;");
        assertSuccessSerialize("class A{int x(){return 1;}} class B extends A{int x(){return 2;} int get(){return super.x();}} assert new B().get() == 1;");

        assertFailCompile("class A{void m1(){} void m2(){super.m1();}}");

        assertFailCompile("super();");
        assertFailCompile("super;");
        assertFailCompile("super = null;");
    }

    @Test
    public void testAsserts() {
        assertSuccessSerialize("assert true;");
        assertSuccessSerialize("assert true : \"failure\";");
        assertFail("assert false;");
        assertFail("assert false : \"failure\";");
        assertFailCompile("assert;");
        assertFailCompile("assert 1;");
        assertFailCompile("assert \"\";");
        assertFailCompile("assert : ;");
        assertFailCompile("assert : \"\";");
        // TODO test assert activity
    }

    @Test
    public void testSynchronized() {
        assertSuccessSerialize("Object o = new Object(); synchronized(o){}");
        assertSuccessSerialize("synchronized(new int[0]){}");
        assertFailCompile("Object o = new Object(); synchronized(o);");
        assertFailCompile("Object o; synchronized(o){}");
        assertFailCompile("synchronized(){}");
        assertFailCompile("int x = 0; synchronized(x){}");
        assertFailCompile("synchronized(1){}");
        assertFailCompile("synchronized(null){}");
        assertFailMessage("Object o = null; synchronized(o){}", "null pointer");
    }

    @Test
    public void testLabels() {
        assertSuccessSerialize("LABEL: {}");
        assertSuccessSerialize("LABEL: {break LABEL;}");
        assertFailCompile("LABEL: {break;}");
        assertSuccessSerialize("LABEL: for(int i = 0; i < 10; i++) {continue LABEL;}");
        assertFailCompile("LABEL: {continue LABEL;}");
        assertFailCompile("LABEL1: {LABEL1: {}}");
        assertFailCompile("break;");
        assertFailCompile("continue;");
        assertFailCompile("break LABEL;");
        assertFailCompile("continue LABEL;");
    }

    @Test
    public void testFails() {
        assertFailCompile("class;");
        assertFailCompile("true;");
        assertFailCompile("1;");
        assertFailCompile("1+2;");
        assertFailCompile("(1);");
        assertFailCompile("(1+2);");
        assertFailCompile("{1}");
    }

    @Test
    public void testNPE() {
        assertFailMessage("class A{int b;} A a = null; int b = a.b;", "null pointer");
    }
}