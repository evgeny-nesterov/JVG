import org.junit.jupiter.api.Test;

public class TestStatements extends HiTest {
	@Test
	public void testBody() {
		assertSuccessSerialize("");
		assertSuccessSerialize("{}");
		assertSuccessSerialize("{};");
		assertSuccessSerialize("{{}} {}");
	}

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

		assertFailCompile("String 0var;", //
				"';' is expected");
		assertFailCompile("String a, a;", //
				"duplicated local variable a");
		assertFailCompile("int a = 1; String a = \"\";", //
				"duplicated local variable a");
		assertFailCompile("int x; int y = x;", //
				"variable 'x' is not initialized");
		assertFailCompile("int x = 1; int y = true || x;", //
				"operator '||' can not be applied to boolean, int");
		assertFailCompile("int x = 1, y = 0.0;", //
				"incompatible types: double cannot be converted to int");
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
		assertSuccessSerialize("ArrayList<String> l = new ArrayList<>(); l.add(\"a\"); l.add(\"b\"); String a = \"\"; for(String s : l){a += s;} assert a.equals(\"ab\");");
		assertSuccessSerialize("ArrayList<String> l = new ArrayList<>(); l.add(\"a\"); l.add(\"b\"); String a = null; for(String s : l){a = s; break;} assert a.equals(\"a\");");
		assertSuccessSerialize("for(int x : new ArrayList<Integer>()){}");
		assertSuccessSerialize("for(Object x : new ArrayList()){}");

		assertFailCompile("for(private int x = 0; x < 10; x++){}", //
				"modifier 'private' is not allowed");
		assertFailCompile("for(private int x : new long[]{1}){}", //
				"modifiers not allowed");
		assertFailCompile("for(int x : new String[]{\"a\", \"b\"}){}", //
				"incompatible types: String cannot be converted to int");
		assertFailCompile("for(int x : new long[]{1L, 2L, 3L}){}", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("for(int x : new ArrayList()){}", //
				"incompatible types: Object cannot be converted to int");
		assertFailCompile("for(int x : new ArrayList<String>()){}", //
				"incompatible types: String cannot be converted to int");
		assertFailCompile("for(Boolean x : new ArrayList<String>()){}", //
				"incompatible types: String cannot be converted to Boolean");

		assertFailCompile("for() {}", //
				"';' is expected");
		assertFailCompile("for(;) {}", //
				"';' is expected");
		assertFailCompile("for(;;;) {}", //
				"')' is expected");
		assertFailCompile("for(;;) {", //
				"'}' is expected");
		assertFailCompile("for(;;)", //
				"statement is expected");
		assertFailCompile("int x = 0; for(;x=1, x=2;) {}", //
				"';' is expected");
		assertFailCompile("for(int x : new Object()) {}", //
				"iterable is expected");
		assertFailCompile("for(int x : true) {}", //
				"iterable is expected");
		assertFailCompile("for(int x : 1) {}", //
				"iterable is expected");
		assertFailCompile("for(int x : \"\") {}", //
				"iterable is expected");
	}

	@Test
	public void testWhile() {
		assertSuccessSerialize("int x = 3; while(x != 0) {x--;}");
		assertSuccessSerialize("int x = 3; while(true) {x--; if(x <= 1) break; else continue; x = 1;} assert x == 1;");

		assertFailCompile("while() {}", //
				"expression expected");
		assertFailCompile("while(1) {}", //
				"boolean is expected");
		assertFailCompile("while(1.1) {}", //
				"boolean is expected");
		assertFailCompile("while(\"\") {}", //
				"boolean is expected");
		assertFailCompile("while(true;) {}", //
				"')' is expected");
		assertFailCompile("while(true)", //
				"statement is expected");
		assertFailCompile("while(true) {", //
				"'}' is expected");
	}

	@Test
	public void testDoWhile() {
		assertSuccessSerialize("int x = 3; do{x--;} while(x != 0); assert x == 0;");
		assertSuccessSerialize("int x = 3; do {x--;} while(x != 0); assert x == 0;");
		assertSuccessSerialize("int x = 3; do {x--; if(x == 1) break;} while(x != 0); assert x == 1;");
		assertSuccessSerialize("int x = 3; do {x--; if(x >= 1) continue; break;} while(true); assert x == 0;");

		assertFailCompile("do {} do(true);", //
				"'while' is expected");
		assertFailCompile("do {} while();", //
				"expression expected");
		assertFailCompile("do {} while(true;);", //
				"')' is expected");
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

		assertFailCompile("if() {}", //
				"expression expected");
		assertFailCompile("if(true;) {}", //
				"')' is expected");
		assertFailCompile("if(true)", //
				"statement is expected");
		assertFailCompile("if(1) {}", //
				"boolean is expected");
		assertFailCompile("if(1.0) {}", //
				"boolean is expected");
		assertFailCompile("if(\"\") {}", //
				"boolean is expected");
		assertFailCompile("if(true) {} else if(){}", //
				"expression expected");
		assertFailCompile("if(true) {} else if(false)", //
				"statement is expected");
		assertFailCompile("if(true) {} else if(false){", //
				"'}' is expected");
		assertFailCompile("if(true) {} else if(false){} else ", //
				"statement is expected");
		assertFailCompile("if(true) {} else if(false){} else {", //
				"'}' is expected");
	}

	@Test
	public void testSwitch() {
		// primitives
		assertSuccessSerialize("switch((byte)1){case (byte)1:}");
		assertSuccessSerialize("switch((short)1){case (short)1:}");
		assertSuccessSerialize("switch((char)1){case (char)1:}");
		assertSuccessSerialize("switch(1){case 1:}");
		assertSuccessSerialize("switch(1){case 1,2,3: break; case 4,5,6: assert false; break; default: assert false; break;}");
		assertSuccessSerialize("int x = 1; switch(x){case 1: assert true;break; case 2: assert false;break;}");
		assertSuccessSerialize("int x = 1; switch(x){case 2: assert false;break; default: assert true;break;}");
		assertSuccessSerialize("int x = 1; switch(x){case 2, x == 1: assert true;break; case 1, x == 2: assert false;break;}");
		assertSuccessSerialize("int x = 2; switch(x) {case 0: x=0; break; case 1: x--; break; case 2, 3, x + 1: x++; break;} assert x == 3;");
		assertSuccessSerialize("int x = 1; switch(x) {case 0: x=0; case 1: x++; case 2: x++;} assert x == 3;");
		assertSuccessSerialize("int x = 4; switch(x) {case 0: x=0; break; case 1: x--; break; case 2: x++; break; default: x += 2;} assert x == 6;");
		assertFailCompile("int x = 1; switch(x){case 1:break; case \"\":break;}", //
				"incompatible switch case types; found String, required int");
		assertFailCompile("switch(1L){}", //
				"invalid switch value type: 'long'");
		assertFailCompile("switch(1F){}", //
				"invalid switch value type: 'float'");
		assertFailCompile("switch(1.0){}", //
				"invalid switch value type: 'double'");
		assertFailCompile("switch(true){}", //
				"invalid switch value type: 'boolean'");
		assertSuccessSerialize("class A{static int get(){System.exit(); return 1;}} switch(A.get()){case 1: break;}");
		assertSuccessSerialize("switch(1){case Boolean.TRUE: return;} assert false;");

		// enums
		assertSuccessSerialize("enum E{e} switch(E.e){}");
		assertSuccessSerialize("enum E{a,b,c} E e = E.a; switch(e){case a: break; case b: assert false;}");
		assertSuccessSerialize("enum E{a,b,c} E e = E.b; switch(e){case a: assert false; case b:}");
		assertSuccessSerialize("enum E{a,b,c} E e = E.b; switch(e){case a: assert false; case b: break; case E.c: assert false;}");
		assertFailCompile("enum E{e} switch(E.e){case :}", //
				"expression expected");
		assertFailCompile("enum E{a,b,c} E e = E.b; switch(e){case a: break; case b: break; case c: break; case d: break;}", //
				"cannot resolve symbol 'd'");
		assertFailCompile("enum E{a,b} switch(E.a){case 1: assert false;} assert false;", //
				"an enum switch case label must be the unqualified name of an enumeration constant");
		assertSuccessSerialize("enum E{a,b} switch(E.a){case b: break; default: return;} assert false;");

		// objects
		assertSuccessSerialize("switch(\"a\"){case \"a\": break; default: assert false;}");
		assertSuccessSerialize("switch(\"a\"){case \"a\",\"b\",\"c\": break; case \"d\",\"e\",\"f\": break; default: break;}");
		assertSuccessSerialize("String s = \"c\"; switch(s) {case \"a\", \"x\": s = \"\"; break; case \"b\": break; case \"c\": s += 1; break;} assert s.equals(\"c1\");");

		assertSuccessSerialize("switch(new Object()){}");
		assertSuccessSerialize("class A {}; class B{}; A a = new A(); Object b = new B(); switch(b) {case a: break; case new A(): break; case b: b = a;} assert b == a && b.equals(a);");
		assertSuccessSerialize("class O{int a; O(int a){this.a=a;}} switch(new O(1)){case O o when o.a == 0: assert false : \"case 1\"; case O o when o.a == 1: assert o.a == 1; return;} assert false : \"case 2\";");
		assertSuccessSerialize("Object s = \"a\"; switch(s){case \"a\": assert true;break; case \"b\": assert false;break;}");

		assertSuccessSerialize("Object o = \"a\"; switch(o){case String s: assert s.equals(\"a\"): \"o=\" + o; break; case Integer i: assert false: \"Integer\"; case Object o2: assert false: \"Object\"; default: assert false: \"default\";}");
		assertSuccessSerialize("Object o = \"a\"; switch(o){case String s1 when s1.length() == 1: assert s1.length() == 1; break; case String s2: assert false: \"length != 1\";}");

		assertFailCompile("class A{static void m(){}} switch(new Object()){case A.m():}", //
				"value or casted identifier is expected");
		assertFailCompile("class A{} class B{} switch(new A()){case B b:}", //
				"incompatible switch case types; found B, required A");
		assertSuccessSerialize("switch(new Integer(1)){case 1:return;} assert false;");
		assertFail("class A{boolean m(){throw new RuntimeException(\"error\");}} switch(new A()){case A a when a.m(): assert false;} assert false;", //
				"error");
		assertSuccessSerialize("Object o = new Integer[0]; switch(o){case String[] s: assert false; case Integer[] i: return;} assert false;");
		assertSuccessSerialize("Object o = new Double[0]; switch(o){case Integer[] s: assert false; case Number[] i: return;} assert false;");

		// casted identifiers
		assertFailCompile("switch(\"\"){case Integer i, String s: break;}", //
				"only one casted identifier is allowed in the case condition");
//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
		assertFail("record R(boolean x){boolean getX(){throw new RuntimeException(\"exception in record rewritten method\");}} switch(new R(true)){case R(boolean x) when x: assert false;} assert false;", //
				"exception in record rewritten method");

		// nulls
		assertSuccessSerialize("switch(null){case null: return; break; case 1:} assert false;");
		assertSuccessSerialize("switch(\"\"){case null: break; case \"\": return;} assert false;");
		assertFailCompile("switch(1){case null: case 1:}", //
				"incompatible switch case types; found null, required int");

		// format failure
		assertFailCompile("switch(){}", //
				"expression expected");
		assertFailCompile("switch(1);", //
				"'{' is expected");
		assertFailCompile("switch(1;){}", //
				"')' is expected");
		assertFailCompile("switch(1,2){}", //
				"')' is expected");
		assertFailCompile("switch(1){case}", //
				"':' is expected");
		assertFailCompile("switch(1){case :}", //
				"expression expected");
		assertFailCompile("switch(1){case 1,2,:break;}", //
				"expression expected");
		assertFailCompile("switch(1){case 1: break}", //
				"';' is expected");
		assertFailCompile("switch(1){case 1: break; default}", //
				"':' is expected");
		assertFailCompile("switch(1){default;}", //
				"':' is expected");
		assertFailCompile("switch(void){}", //
				"expression expected");
		assertFailCompile("switch(int){}", //
				"expression expected");
		assertFailCompile("switch(int x = 1){}", //
				"expression expected");

		// failures
		assertFailCompile("class A{} switch(A){}", //
				"expression expected");
		assertFailCompile("class A{void m(){}} switch(new A().m()){}", //
				"value is expected");
		assertFail("class A{int get(){throw new RuntimeException(\"error\");}} switch(new A().get()){case 1: break;}", //
				"error");
		assertFailCompile("switch(1){case 1,2,1: break;}", //
				"case value '1' is duplicated");
		assertFailCompile("switch(1){case 1,2,3: break; case 3: break;}", //
				"case value '3' is duplicated");
		assertFailCompile("switch(12){case 1, 12, 2*5+2: break;}", //
				"case value '12' is duplicated");
		assertFailCompile("switch(12){case 1, ((2 * 8 + 18/3)/2-1)/2, 5: break;}", //
				"case value '5' is duplicated");
		assertFailCompile("switch(\"a\"){case \"a\",\"b\",\"a\": break;}", //
				"case value 'a' is duplicated");
		assertFailCompile("switch(\"a\"){case \"ab\",\"b\",\"a\"+\"b\": break;}", //
				"case value 'ab' is duplicated");
		assertFailCompile("switch(\"a\"){case \"a\": break; case \"b\", \"a\": break;}", //
				"case value 'a' is duplicated");
		assertFailCompile("enum E{e1,e2,e3} switch(E.e1){case e1,e2,e3,e1: break;}", //
				"case enum value 'e1' is duplicated");
		assertFailCompile("enum E{e1,e2,e3} switch(E.e1){case e1,e2,e3: break; case e2: break;}", //
				"case enum value 'e2' is duplicated");
		assertFailCompile("switch(null){case null, null:}", //
				"case value 'null' is duplicated");
		assertFailCompile("switch(null){case null: break; case null: break;}", //
				"case value 'null' is duplicated");
	}

	@Test
	public void testExceptions() {
		assertSuccessSerialize("new Exception(); new Exception(\"error\"); new RuntimeException(); new RuntimeException(\"error\");");
		assertSuccessSerialize("class E extends Exception {E(){} E(String msg){super(msg);}}; E e1 = new E(); E e2 = new E(\"error\"); assert \"error\".equals(e2.getMessage()); assert e1 instanceof Exception;");

		// throw in root
		assertFailCompile("throw new Exception(\"error\");", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("throw new Exception(null);", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("throw new Exception(1, 2, 3);", //
				"constructor not found: Exception");
		assertFail("throw new RuntimeException(\"error\");", //
				"error");
		assertFailCompile("throw new Object();", //
				"incompatible types: Object cannot be converted to Exception");
		assertFailCompile("throw \"\";", //
				"incompatible types: String cannot be converted to Exception");
		assertFailCompile("throw 1;", //
				"incompatible types: int cannot be converted to Exception");
		assertFailCompile("throw true;", //
				"incompatible types: boolean cannot be converted to Exception");
		assertFailCompile("class E extends Exception{} throw new E();", //
				"unreported exception E: exception must be caught or declared to be thrown");
		assertFail("class E extends RuntimeException{} throw new E();");
		assertFail("Exception exc = null; try {throw new RuntimeException(\"error\");} catch(Exception e) {exc = e;} finally {if (exc != null) throw exc;}", //
				"error");

		// throw in try
		assertSuccessSerialize("try{throw new Exception(\"error\");} catch(Exception e){assert e.getMessage().equals(\"error\");} ");
		assertSuccessSerialize("class E extends Exception{E(String message){super(message);}} try {throw new E(\"error\");} catch(E e) {assert e.getMessage().equals(\"error\");} ");
		assertFailCompile("class E extends Exception{} try{throw new Exception();} catch(E e) {}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailSerialize("class E extends RuntimeException{} try{throw new RuntimeException();} catch(E e) {}");
		assertSuccessSerialize("class E extends Exception{} try{throw new Exception();} catch(E e){assert false;} catch(RuntimeException e){assert false;} catch(Exception e){}");
		assertSuccessSerialize("class E extends Exception {E(String message){super(message);}} " + //
				"class A {void m(int x) throws E {if (x == 1) throw new E(\"error-\" + x);}}" + //
				"try {A a = new A(); a.m(1);} catch(E e) {assert e.getMessage().equals(\"error-1\");}");
		assertSuccessSerialize("class A implements AutoCloseable{int x = 1; public void close(){x--;}} A a_; try(A a = a_= new A()) {assert a.x==1;} finally{assert a_.x==0;} assert a_.x==0;");
		assertSuccessSerialize("interface I extends AutoCloseable{} class A implements I{public void close(){}} try(A a1 = new A(); A a2 = new A()) {}");
		assertFailCompile("class A implements AutoCloseable{public void close(){}} try(A a = new A();) {}", //
				"declaration expected");
		assertSuccessSerialize("class E1 extends Exception{E1(){} E1(String msg){super(msg);}} class E2 extends Exception{E2(){} E2(String msg){super(msg);}} try{throw new E2(\"error\");} catch(E1 | E2 e){assert e.getMessage().equals(\"error\");}");
		assertFail("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"close error\");}} try(A a = new A()) {}", //
				"close error");
		assertFail("class A implements AutoCloseable{public void close(){}} try(A a = null) {}", //
				"null pointer");

		// Exception has already been caught
		assertFailCompile("try{} catch(Exception e){} catch(Exception e){}", //
				"exception 'Exception' has already been caught");
		assertFailCompile("try{} catch(Exception e){} catch(RuntimeException e){}", //
				"exception 'RuntimeException' has already been caught");
		assertFailCompile("try{} catch(RuntimeException e){} catch(RuntimeException e){}", //
				"exception 'RuntimeException' has already been caught");
		assertFailCompile("class E extends Exception{} try{} catch(E e){} catch(E e){}", //
				"exception 'E' has already been caught");
		assertFailCompile("class E extends Exception{} try{} catch(Exception e){} catch(E e){}", //
				"exception 'E' has already been caught");
		assertFailCompile("try{} catch(Exception | Exception e){}", //
				"types in multi-catch must be disjoint: 'Exception' is a subclass of 'Exception'");
		assertFailCompile("try{} catch(Exception | RuntimeException e){}", //
				"types in multi-catch must be disjoint: 'RuntimeException' is a subclass of 'Exception'");
		assertFailCompile("try{} catch(RuntimeException | Exception e){}", //
				"types in multi-catch must be disjoint: 'RuntimeException' is a subclass of 'Exception'");

		// in constructor
		assertFailCompile("class A{A() {throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("class A{A() throws RuntimeException {throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("class A{A() throws Exception {throw new Exception(); super();}}", //
				"';' is expected");
		assertFailCompile("class A{A() throws String {}}", //
				"incompatible types: String cannot be converted to Exception");
		assertFail("class A{A(){throw new RuntimeException(\"runtime\");}} new A();", //
				"runtime");
		assertFail("class A{A(){throw new RuntimeException(\"runtime\");}} class B extends A{B(){super();}} new B();", //
				"runtime");

		// in method
		assertFailCompile("class A{void m() {throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("class A{void m() throws RuntimeException {throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFailCompile("class A{void m() throws String {}}", //
				"incompatible types: String cannot be converted to Exception");
		assertFail("class A{void m(){throw new RuntimeException(\"runtime\");}} new A().m();", //
				"runtime");

		// in initialization
		assertFailCompile("class A{{throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertFail("class A{{throw new RuntimeException(\"runtime\");}} new A();", //
				"runtime");

		assertFailCompile("class A{static{throw new Exception();}}", //
				"unreported exception Exception: exception must be caught or declared to be thrown");
		assertSuccessSerialize("class A{static{throw new RuntimeException();}}");
		assertFail("class A{static int x = 1; static{throw new RuntimeException(\"runtime\");}} int x = A.x;", //
				"runtime");
		assertFailCompile("try(int x = 1) {}", //
				"incompatible types: found: 'int', required: 'AutoCloseable'");
		assertFailCompile("try(Object x = new Object()) {}", //
				"incompatible types: found: 'Object', required: 'AutoCloseable'");
		assertFailCompile("try {} catch(String | Integer e){}", //
				"incompatible types: String cannot be converted to Exception");
		assertFailCompile("try {} catch(RuntimeException | Integer e){}", //
				"incompatible types: Integer cannot be converted to Exception");

		assertSuccessSerialize("class A{int m(){try {throw new RuntimeException(\"1\");} catch(Exception e) {} finally{return 1;}}} assert new A().m() == 1;");
		assertSuccessSerialize("class A{int m(){try {throw new RuntimeException(\"error 1\");} catch(Exception e) {throw new RuntimeException(\"error 2\");} finally{return 1;}}} assert new A().m() == 1;"); // cancel exception and return 1 in finally

		// resource initialization
		assertFail("class A implements AutoCloseable{A(){throw new RuntimeException(\"exception in resource initialization\");} public void close(){}} try(A a = new A()){}", //
				"exception in resource initialization");
		// resource close
		assertFail("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"exception in resource close\");}} try(A a = new A()){}", //
				"exception in resource close");
		assertSuccessSerialize("class A implements AutoCloseable{public void close(){throw new RuntimeException(\"exception in resource close\");}} try(A a = new A()){} catch(Exception e){assert e.getMessage().equals(\"exception in resource close\");}");

		// invalid format
		assertFailCompile("throw Exception();", //
				"cannot resolve method 'Exception'");
		assertFailCompile("throw new;", //
				"identifier is expected");
		assertFailCompile("throw new Exception;", //
				"expression expected");
		assertFailCompile("try()", //
				"declaration expected");
		assertFailCompile("try(){}", //
				"declaration expected");
		assertFailCompile("try{} catch(Exception ){}", //
				"identifier is expected");
		assertFailCompile("try{} catch(Exception | e){}", //
				"identifier is expected");
		assertFailCompile("try{} catch(Exception e){} finally", //
				"'{' is expected");
		assertFailCompile("try(Object x) {}", //
				"'=' is expected");
		assertFailCompile("try(Object x = new Object()) {}", //
				"incompatible types: found: 'Object', required: 'AutoCloseable'");
		assertFailCompile("try(String x = \"abc\") {}", //
				"incompatible types: found: 'String', required: 'AutoCloseable'");
		assertFailCompile("try{} catch(Object e){}", //
				"incompatible types: Object cannot be converted to Exception");
		assertFailCompile("try{} catch(Integer | Long | Byte e){}", //
				"incompatible types: Integer cannot be converted to Exception");
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

		assertFailCompile("boolean[] x = new boolean[]{1};", //
				"boolean is expected");
		assertFailCompile("byte[] x = new byte[]{true};", //
				"byte is expected");
		assertFailCompile("byte[] x = new byte[]{1L};", //
				"byte is expected");
		assertFailCompile("byte[] x = new byte[]{1D};", //
				"byte is expected");
		assertFailCompile("byte[] x = new byte[]{1F};", //
				"byte is expected");
		assertFailCompile("short[] x = new short[]{true};", //
				"short is expected");
		assertFailCompile("short[] x = new short[]{1L};", //
				"short is expected");
		assertFailCompile("short[] x = new short[]{1D};", //
				"short is expected");
		assertFailCompile("short[] x = new short[]{1F};", //
				"short is expected");
		assertFailCompile("char[] x = new char[]{true};", //
				"char is expected");
		assertFailCompile("char[] x = new char[]{1L};", //
				"char is expected");
		assertFailCompile("char[] x = new char[]{1D};", //
				"char is expected");
		assertFailCompile("char[] x = new char[]{1F};", //
				"char is expected");
		assertFailCompile("int[] x = new int[]{true};", //
				"int is expected");
		assertFailCompile("int[] x = new int[]{1L};", //
				"int is expected");
		assertFailCompile("int[] x = new int[]{1D};", //
				"int is expected");
		assertFailCompile("int[] x = new int[]{1F};", //
				"int is expected");
		assertFailCompile("long[] x = new long[]{true};", //
				"long is expected");
		assertFailCompile("long[] x = new long[]{1D};", //
				"long is expected");
		assertFailCompile("long[] x = new long[]{1F};", //
				"long is expected");
		assertFailCompile("float[] x = new float[]{true};", //
				"float is expected");
		assertFailCompile("float[] x = new float[]{1D};", //
				"float is expected");
		assertFailCompile("double[] x = new double[]{true};", //
				"double is expected");

		assertFailCompile("int x = 0; x.new Object();", //
				"identifier is expected");
		assertFailCompile("int[] x = {0}; x.new Object();", //
				"identifier is expected");
		assertFailCompile("class A{class B{}} A[] a = {new A()}; a.new B();", //
				"identifier is expected");
	}

	@Test
	public void testBreakContinueLabel() {
		assertSuccessSerialize("A:{break A;};");
		assertSuccessSerialize("A:{{{break A;}}};");
		assertSuccessSerialize("A:break A;");
		assertSuccessSerialize("A:for(int i=0; i< 3; i++){continue A;};");
		assertFailCompile("A:{continue A;};", //
				"undefined label 'A'");
		assertSuccessSerialize("A:for(int i=0; i< 3; i++){{{continue A;}}};");
		assertSuccessSerialize("A:for(int i=0; i< 3; i++) continue A;");
		assertFailCompile("A:continue A;", //
				"undefined label 'A'");
		assertSuccessSerialize("A:B:C:D:{break B;}");
		assertFailCompile("break A;", //
				"undefined label 'A'");
		assertFailCompile("A:{} break A;", //
				"undefined label 'A'");
		assertFailCompile("A:{break B;}", //
				"undefined label 'B'");
		assertFailCompile("A:{continue B;}", //
				"undefined label 'B'");

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
		assertSuccessSerialize("class A{long m(){return 1l;}}");
		assertSuccessSerialize("class A{float m(){return 1l;}}");
		assertSuccessSerialize("class A{double m(){return 1l;}}");
		assertSuccessSerialize("class A{float m(){return 1f;}}");
		assertSuccessSerialize("class A{double m(){return 1f;}}");
		assertSuccessSerialize("class A{double m(){return 1.0;}}");
		assertFailCompile("class A{int m(){return \"\";}}", //
				"incompatible types; found String, required int");
		assertFailCompile("class A{int m(){return 1L;}}", //
				"incompatible types; found long, required int");
		assertFailCompile("class A{int m(){return;}}", //
				"incompatible types; found void, required int");
		assertFailCompile("class A{void m(){return; int x = 1;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(){return; return;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(){return A;}}", //
				"value is expected");

		assertSuccessSerialize("class A{void m(){return;}} new A().m();");
		assertFailCompile("class A{void m(){return;}} Object x = new A().m();", //
				"incompatible types: void cannot be converted to Object");
		assertFailCompile("class A{void m(){return 1;}}", //
				"incompatible types; found int, required void");

		assertSuccessSerialize("class A{int m(){ if(true) {{{return 1;}}} else return 2; }} assert new A().m() == 1;");
		assertSuccessSerialize("class A{int m(int x) {if(x == 3) return x; return 0;}} assert new A().m(3) == 3;");

		// unreachable statement
		assertFailCompile("class A{void m(){try{return;} catch(Exception e){return;}; int y = 0;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(boolean x){if(x) {return;} else if(!x) {return;} else {return;}; int y = 0;}}", //
				"unreachable statement");
		assertFailCompile("class A{void m(int x){switch(x) {case 1: return; case 2: return; default: return;}; int y = 0;}}", //
				"unreachable statement");

		// constructors
		assertSuccessSerialize("class A{A(){return;}}");
		assertFailCompile("class A{A(){return \"\";}}", //
				"incompatible types; found String, required void");
		assertFailCompile("class A{A(){return; int x = 1;}}", //
				"unreachable statement");
		assertFailCompile("class A{A(){return; return;}}", //
				"unreachable statement");

		assertSuccessSerialize("class A{int x = 0; A(){if(true) return; x = 1;}} assert new A().x == 0;");

		// initializers
		assertSuccessSerialize("class A{{int x = 0; return;}}");
		assertSuccessSerialize("class A{static{int x = 0; return;}}");
		assertFailCompile("class A{{return \"\";}}", //
				"incompatible types; found String, required void");
		assertFailCompile("class A{{return; int x = 0;}}", //
				"unreachable statement");
		assertFailCompile("class A{static{return; return;}}", //
				"unreachable statement");
		assertFailCompile("class A{static{{return;} return;}}", //
				"unreachable statement");

		assertSuccessSerialize("class A{int x = 0; {if(true) return; x = 1;}} assert new A().x == 0;");
		assertSuccessSerialize("class A{int m(){try {return 1;} catch(Exception e) {return 2;} finally {return 3;}}} assert new A().m() == 3;");
		assertSuccessSerialize("class A{int m(){try {throw new Exception();} catch(Exception e) {return 2;} finally {return 3;}}} assert new A().m() == 3;");
	}

	@Test
	public void testUnreachable() {
		// return and throw
		assertFailCompile("class C{int m(){{return 1;} return 2;}}", //
				"unreachable statement");
		assertFailCompile("class C{int m(){do {return 1;} while(true); return 2;}}", //
				"unreachable statement");
		assertFailCompile("class C{int m(){synchronized(this) {return 1;} return 2;}}", //
				"unreachable statement");
		assertFailCompile("class C{int m(){BLOCK:{return 1;} return 2;}}", //
				"unreachable statement");
		assertFailCompile("class C{int m(){if(false) while(true) {{return 1;} return 2;}}}", //
				"unreachable statement");

		assertFailCompile("class C{int m(){{{return 1;} return 3;} return 4;}}", //
				"unreachable statement");
		assertFailCompile("class C{void m() {{{{return;} return;} return;} return;}}", //
				"unreachable statement");
		assertFailCompile("class C{void m() throws Exception {{{{return;} return;} throw new Exception();} return;}}", //
				"unreachable statement");

		assertFailCompile("class C{void m(){BLOCK:{break BLOCK; return;} return;}}", //
				"unreachable statement");
		assertSuccessSerialize("class C{void m(){BLOCK:{break BLOCK;} return;}}");
		assertFailCompile("class C{void m(){BLOCK:{continue BLOCK; return;} return;}}", //
				"unreachable statement");

		// break and continue
		assertFailCompile("int x = 1; switch(x) {case 1: break; break;}", //
				"unreachable statement");
		assertFailCompile("int x = 1; switch(x) {case 1: {break;} break;}", //
				"unreachable statement");
		assertFailCompile("int x = 1; switch(x) {case 1: BLOCK:{break;} break;}", //
				"unreachable statement");
		assertFailCompile("int x = 1; switch(x) {case 1: synchronized(this){break;} break;}", //
				"unreachable statement");
		assertSuccessSerialize("int x = 1; switch(x) {case 1: do{break;}while(true); break;}");

		// switch
		assertSuccessSerialize("int x = 1; switch(x) {case 1: break; case 2: break;} return;");
		assertSuccessSerialize("int x = 1; switch(x) {case 1: return; case 2: return;} return;");
		assertSuccessSerialize("int x = 1; switch(x) {case 1: return; default: break;} return;");
		assertFailCompile("int x = 1; switch(x) {case 1: return; default: return;} return;", //
				"unreachable statement");
		assertFailCompile("int x = 1; BLOCK:{switch(x) {case 1: break BLOCK; default: break BLOCK;} break BLOCK;}", //
				"unreachable statement");
		assertFailCompile("WHILE: while(true) {switch(x) {case 1: continue WHILE; default: continue WHILE;} int y = 0;}", //
				"unreachable statement");

		// if
		assertSuccessSerialize("if(true) {return;} return;");
		assertSuccessSerialize("if(true) {return;} else if(false) {return;} return;");
		assertSuccessSerialize("if(true) {return;} else if(false) {return;} else {} return;");
		assertFailCompile("if(true) {return;} else if(false) {return;} else {return;} return;", //
				"unreachable statement");

		// try
		assertSuccessSerialize("try {return;} catch(Exception e){} return;");
		assertSuccessSerialize("try {} catch(Exception e){return;} return;");
		assertFailCompile("try {} catch(Exception e){} finally{return;} return;", //
				"unreachable statement");
		assertFailCompile("try {return;} catch(Exception e){} finally{return;} return;", //
				"unreachable statement");
		assertFailCompile("try {} catch(Exception e){return;} finally{return;} return;", //
				"unreachable statement");
		assertFailCompile("try {return;} catch(Exception e){return;} finally{return;} return;", //
				"unreachable statement");
	}

	@Test
	public void testThis() {
		assertSuccessSerialize("class A{{Object a = this; assert a instanceof A;}} new A();");
		assertSuccessSerialize("class A{void m(){Object a = A.this; assert a instanceof A;}} new A().m();");
		assertSuccessSerialize("class A{int x = 1; {this.x = 2;}} assert new A().x == 2;");
		assertSuccessSerialize("class A{int x = 1; {A.this.x = 2;}} assert new A().x == 2;");
		assertSuccessSerialize("class A{int x(){return 1;} int y(){return this.x() + 1;}} assert new A().y() == 2;");
		assertSuccessSerialize("class A{int x(){return 1;} int y(){return A.this.x() + 1;}} assert new A().y() == 2;");
		assertFailCompile("this();", //
				"expression expected"); // TODO Call to 'this()' must be first statement in constructor body
		assertFailCompile("this;", //
				"not a statement");
		assertFailCompile("this = null;", //
				"variable expected");
	}

	@Test
	public void testSuper() {
		assertSuccessSerialize("class A{int x = 1;} class B extends A{int x = 2; int get(){return super.x;}} assert new B().get() == 1;");
		assertSuccessSerialize("class A{int x(){return 1;}} class B extends A{int x(){return 2;} int get(){return super.x();}} assert new B().get() == 1;");

		assertFailCompile("class A{void m1(){} void m2(){super.m1();}}", //
				"cannot resolve method 'm1' in 'Object'");
		assertFailCompile("super();", //
				"expression expected");
		assertFailCompile("super;", //
				"not a statement");
		assertFailCompile("super = null;", //
				"variable expected");
	}

	@Test
	public void testAsserts() {
		assertSuccessSerialize("assert true;");
		assertSuccessSerialize("assert true : \"failure\";");
		assertFail("assert false;", //
				"Assert failed");
		assertFail("assert false : \"failure\";", //
				"failure");
		assertFailCompile("assert;", //
				"expression expected");
		assertFailCompile("assert 1;", //
				"boolean is expected");
		assertFailCompile("assert \"\";", //
				"boolean is expected");
		assertFailCompile("assert : ;", //
				"expression expected");
		assertFailCompile("assert : \"\";", //
				"expression expected");
		// TODO test assert activity
	}

	@Test
	public void testSynchronized() {
		assertSuccessSerialize("Object o = new Object(); synchronized(o){}");
		assertSuccessSerialize("synchronized(new int[0]){}");
		assertFailCompile("Object o = new Object(); synchronized(o);", //
				"'{' is expected");
		assertFailCompile("Object o; synchronized(o){}", //
				"variable 'o' is not initialized");
		assertFailCompile("synchronized(){}", //
				"expression expected");
		assertFailCompile("int x = 0; synchronized(x){}", //
				"object is expected");
		assertFailCompile("synchronized(1){}", //
				"object is expected");
		assertFailCompile("synchronized(null){}", //
				"object is expected");
		assertFail("Object o = null; synchronized(o){}", //
				"null pointer");
	}

	@Test
	public void testLabels() {
		assertSuccessSerialize("LABEL: {}");
		assertSuccessSerialize("LABEL: {break LABEL;}");
		assertFailCompile("LABEL: {break;}", //
				"break outside switch or loop");
		assertSuccessSerialize("LABEL: for(int i = 0; i < 10; i++) {continue LABEL;}");
		assertFailCompile("LABEL: {continue LABEL;}", //
				"undefined label 'LABEL'");
		assertFailCompile("LABEL1: {LABEL1: {}}", //
				"label 'LABEL1' already in use");
		assertFailCompile("break;", //
				"break outside switch or loop");
		assertFailCompile("continue;", //
				"continue outside of loop");
		assertFailCompile("break LABEL;", //
				"undefined label 'LABEL'");
		assertFailCompile("continue LABEL;", //
				"undefined label 'LABEL'");
	}

	@Test
	public void testFails() {
		assertFailCompile("class;", //
				"class name is expected");
		assertFailCompile("true;", //
				"not a statement");
		assertFailCompile("1;", //
				"not a statement");
		assertFailCompile("1+2;", //
				"not a statement");
		assertFailCompile("(1);", //
				"not a statement");
		assertFailCompile("(1+2);", //
				"not a statement");
		assertFailCompile("{1}", //
				"not a statement");
	}

	@Test
	public void testNPE() {
		assertFail("class A{int b;} A a = null; int b = a.b;", //
				"null pointer");
	}
}