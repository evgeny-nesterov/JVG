import org.junit.jupiter.api.Test;

public class TestLambda extends HiTest {
	@Test
	public void testField() {
		assertSuccess("interface I{void get();} I o = () -> {}; o.get();");
		assertSuccess("interface I{int get(int x);} I o = x -> x + 1; assert o.get(1) == 2;");
		assertSuccess("interface I{int get(int x);} I o = (x) -> {x++; return x;}; assert o.get(1) == 2;");
		assertSuccess("interface I{int get(int x);} I o = (int x) -> {return x + 1;}; assert o.get(1) == 2;");
		assertSuccess("interface I{int get(int x, int y);} I o = (x, y) -> x + y; assert o.get(1, 2) == 3;");
		assertSuccess("interface I{int get(int x, int y);} I o = (int x, int y) -> x + y; assert o.get(1, 2) == 3;");
		assertFailCompile("interface I{int get(int x, int y);} I o = (int x, y) -> 0;", //
				"argument is expected");
		assertFailCompile("interface I{int get(int x, int y);} I o = (x, int y) -> 0;", //
				"unexpected token");
		assertSuccess("interface I{String get(long x, String y);} I o = (x, y) -> x + y; assert o.get((byte)1, \"_\").equals(\"1_\");");
		assertFailCompile("Object o = (x) -> x;", //
				"target type of a lambda conversion must be an interface");
		assertFailCompile("interface I{int get(int x);} I o = (x, y) -> x + y;", //
				"incompatible parameters signature in lambda expression");
		assertFailCompile("interface I{int get(int x);} I o = (boolean x) -> x ? 1 : 0;", //
				"incompatible parameters signature in lambda expression");
		assertFail("interface I{int get(int x, int y);} I o = (x, y) -> x / y; o.get(1, 0);", //
				"divide by zero");
		assertFailCompile("interface I{void get();} I o = () -> 1;", //
				"incompatible types; found int, required void");
		assertSuccess("int x = 1; interface I{int get(int x);} I o = x -> x; assert o.get(x + 1) == x + 1;");
		assertSuccess("class C{int c = 0;} C c = new C(); interface I{void m(I i, int x);} I o = (i,x)->{if(x>0) i.m(i, x-1); c.c++;}; o.m(o, 5); assert c.c == 6;");
		assertSuccess("interface I{int get(int a);} interface J{int sum(int a, I... i);} J j = (a,i) -> {int c = 0; for(int x=0;x<i.length;x++) c+=i[x].get(a); return c;} assert j.sum(3, a->a, a->1, a->a/2) == 5;");
		assertSuccess("class A{interface B{int get();} int m(){ return new A() {int m() {B b = ()->123; return b.get();}}.m(); }} assert new A().m() == 123;");

		assertFailCompile("interface A{void m(int x); void m(String x);} A a = (x) -> {};", //
				"multiple non-overriding abstract methods found in interface A");
		assertFailCompile("interface A{void m1(int x); void m2(String x);} A a = (x) -> {};", //
				"multiple non-overriding abstract methods found in interface A");
		assertFailCompile("interface A{} A a = (x) -> {};", //
				"no abstract methods found in interface A");
		assertFailCompile("var r = ()->{};", //
				"cannot infer type: lambda expression requires an explicit target type");

		// lambda + var
		assertSuccess("interface A{void m(int x);} class B{A a = (var x) -> {};}");
	}

	@Test
	public void testMethodArgument() {
		assertSuccess("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{});");
		assertSuccess("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} assert new C().m((x,y)->x+y,1,2)==3; assert new C().m((x,y)->{return x+y;},1,2)==3;");
		assertSuccess("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} assert new C().m((s,x)->s+x.length).equals(\"l=3\");");

		assertSuccess("interface I{void get();} I i = () -> {};");
		assertSuccess("interface I{void get();} I i = () -> {return;};");
		assertSuccess("interface I{int get(int x);} I i = (int X) -> X+1; assert i.get(1) == 2;");
		assertSuccess("interface I{int get(int x);} I i = (int X) -> {return X+1;}; assert i.get(1) == 2;");
		assertSuccess("interface I{int m1(int a);} class A{int m2(int x){I i = (y) -> y + 1; return i.m1(x);}} assert new A().m2(1) == 2;");

		assertFailCompile("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{return 1;});", //
				"incompatible types; found int, required void");
		assertFailCompile("interface I{void get(int x);} class C{void m(I i){i.get(1);}} new C().m(x->x);", //
				"incompatible types; found int, required void");
		assertFailCompile("interface I{int get();} class C{int m(I i){return i.get();}} new C().m(()->{});", //
				"incompatible types; found void, required int");
		assertFailCompile("interface I{String get(int x);} class C{String m(I i){return i.get(1);}} new C().m(x->{int y = x + 1;});", //
				"incompatible types; found void, required String");
		assertFailCompile("interface I{String get(int x, String... args);} class C{String m(I i){return i.get(\"\", 1, 2, 3);}}", //
				"cannot resolve method 'get' in 'I'");
		assertFailCompile("interface I{String get(int x, String... args);} I i = null; i.get(\"\", 1, 2, 3);", //
				"cannot resolve method 'get' in 'I'");
	}

	@Test
	public void testConstructorArgument() {
		assertSuccess("interface I{void get();} class C{C(I i){i.get();}} new C(()->{});");
		assertSuccess("interface I{int get(int x,int y);} class C{int x; C(I i,int x,int y){this.x=i.get(x,y);}} assert new C((x,y)->x+y,1,2).x==3; assert new C((x,y)->{return x+y;},1,2).x==3;");
		assertSuccess("interface I{String get(String s, int... x);} class C{String s; C(I i){s=i.get(\"l=\",1,2,3);}} assert new C((s,x)->s+x.length).s.equals(\"l=3\");");
		assertFailCompile("interface I{void get();} class C{C(I i){i.get();}} new C(()->{return 1;});", //
				"incompatible types; found int, required void");
		assertFailCompile("interface I{void get(int x);} class C{C(I i){i.get(1);}} new C(x->x);", //
				"incompatible types; found int, required void");
		assertFailCompile("interface I{int get();} class C{C(I i){i.get();}} new C(()->{});", //
				"incompatible types; found void, required int");
		assertFailCompile("interface I{String get(int x);} class C{C(I i){i.get(1);}} new C(x->{int y = x + 1;});", //
				"incompatible types; found void, required String");
		assertFailCompile("interface I{String get(int x, String... args);} class C{C(I i){return i.get(\"\", 1, 2, 3);}}", //
				"cannot resolve method 'get' in 'I'");
	}

	@Test
	public void testArray() {
		assertSuccess("interface A{int get(int x);} A[][] a = {{x->x+1}}; assert a[0][0].get(1) == 2;");
		assertSuccess("interface A{int get(int x);} A[][] a = {{(int x)->x+1}}; assert a[0][0].get(1) == 2;");
		assertSuccess("interface A{int get(int x);} A[][] a = {{(int x)->{return x+1;}}}; assert a[0][0].get(1) == 2;");
		assertSuccess("interface A{int get(int x);} A[][] a = {{null}}; a[0][0] = (var x)->x+1; assert a[0][0].get(1) == 2;");
		assertFailCompile("interface A{int get(int x);} A[][] a = {{x->x==0}};", //
				"incompatible types; found boolean, required int");
		assertFailCompile("interface A{int get(int x, int y);} A[][][] a = {{{x->0}}};", //
				"incompatible parameters signature in lambda expression");
		assertFailCompile("interface A{int get(int x, int y);} A[][][] a = {{{x->{return 0;}}}};", //
				"incompatible parameters signature in lambda expression");
		assertFailCompile("interface A{void get(int x, int y);} A[][][] a = {{{x->{}}}};", //
				"incompatible parameters signature in lambda expression");
		assertFailCompile("interface A{void get();} A[][][] a = {{{x->{}}}};", //
				"incompatible parameters signature in lambda expression");
		assertFailCompile("interface A{int get(int x);} A[][] a = {{(byte x)->0}};", //
				"incompatible parameter types in lambda expression: expected int but found byte");
	}

	@Test
	public void testMethodReferenceDeclaration() {
		// string
		assertSuccess("interface A{int length();} A a = \"abc\"::length; assert a.length() == 3;"); // same method name
		assertSuccess("interface A{int indexOf(String s);} A a = \"abc\"::indexOf; assert a.indexOf(\"b\") == 1;"); // same method name
		assertSuccess("interface A{int getLength();} A a = \"abc\"::length; assert a.getLength() == 3;"); // another method name
		assertSuccess("interface A{int getIndex(String s);} A a = \"abc\"::indexOf; assert a.getIndex(\"c\") == 2;"); // another method name
		assertFailCompile("interface A{int getLength();} A a = \"abc\"::length; assert a.length() == 3;", //
				"cannot resolve method 'length' in 'A'"); // method name is not match
		assertFailCompile("interface A{int length(); int size();} A a = \"abc\"::length;", //
				"functional interface not match to 'A'"); // not a functional interface

		// object
		assertSuccess("interface A{void get();} class C{void get(){}}; C c = new C(); A a = c::get; a.get();");
		assertSuccess("interface A{void getX(int... x); default void getX2(int... x){} static void getX3(int... x){}} class C{void getX(int... x){}}; C c = new C(); A a = c::getX; a.getX(1, 2, 3);");
		assertSuccess("interface A{void get();} class C{static void get(){}}; A a = C::get; a.get();");
		assertFailCompile("interface A{void get(); void get2();} class C{void m(){}}; A a = new C()::m;", //
				"functional interface not match to 'A'");
		assertFailCompile("interface A{void get(); void get(int x);} class C{void m(){}}; A a = new C()::m;", //
				"functional interface not match to 'A'");
		assertFailCompile("interface A{} class C{void m(){}}; A a = new C()::m;", //
				"functional interface not match to 'A'");
		assertFailCompile("abstract class A{abstract void get();} class C{void get(){}}; A a = new C()::get;", //
				"functional interface not match to 'A'");
		assertSuccess("interface A{void get();} class C{void get(){assert false;} void m(){}}; A a = new C()::m;");
		assertSuccess("interface A{void get();} class C{static void get(){} static void m(){}}; A a = C::m;"); // static method

		assertSuccess("interface A1{void get();} interface A2{void get(int x);} class C{void get(){} void get(int x){}}; C c = new C(); A1 a1 = c::get; a1.get(); A2 a2 = c::get; a2.get(1);");
		assertSuccess("interface A1{void get();} interface A2{void get(int x);} class C{static void get(){} static void get(int x){}}; A1 a1 = C::get; a1.get(); A2 a2 = C::get; a2.get(1);");

		assertSuccess("interface A{int get(int x);} class C{int c = 1; int get(int x){return c + x + 1;}}; A a = new C()::get; assert a.get(1) == 3;");
		assertSuccess("interface A{int get(int x);} class C{static int c = 1; static int get(int x){return c + x + 1;}}; A a = C::get; assert a.get(1) == 3;");

		assertSuccess("interface A1{int get();} interface A2{int get(int x);} class C{int get(){return 1;} int get(int x){return x;}}; C c = new C(); A1 a1 = c::get; assert a1.get() == 1; A2 a2 = c::get; assert a2.get(2) == 2;");
		assertSuccess("interface A1{int get();} interface A2{int get(int x);} class C{static int get(){return 1;} static int get(int x){return x;}}; A1 a1 = C::get; assert a1.get() == 1; A2 a2 = C::get; assert a2.get(2) == 2;");
		assertSuccess("interface I{int m1(int a);} class A{class B{int M(int m){return m+1;}} int m2(int x){B b = new B(); I i = b::M; return i.m1(x);}} assert new A().m2(1) == 2;");

		// varargs
		assertSuccess("interface A{int get(int... x);} class B{int get(int... x){return x[x.length - 1];}} A a = B::get; assert a.get(1,2,3) == 3;"); // same method name
		assertSuccess("interface A{int getA(int... x);} class B{int getB(int... x){return x[x.length - 1];}} B b = new B(); A a = b::getB; assert a.getA(1,2,3) == 3;"); // different name method name

		// extends
		assertSuccess("interface A{int get();} interface B extends A{} abstract class C{int get(){return 1;}}; class D extends C{}; D d = new D(); B b = d::get; assert b.get() == 1;");
		assertSuccess("interface A{int get();} interface B extends A{} abstract class C{abstract int get();}; class D extends C{int get(){return 1;}}; D d = new D(); B b = d::get; assert b.get() == 1;");
		assertSuccess("interface A{int get();} interface B extends A{} class C implements A{int get(){return 1;}}; C c = new C(); B b = c::get; assert b.get() == 1;");
		assertSuccess("interface A{int get(); int get2();} interface B extends A{default int get2(){return 2;}} class C{int get1(){return 1;}}; B b = new C()::get1; assert b.get() == 1;");

		// failures
		assertFailCompile("interface A{int notExists();} A a = \"abc\"::notExists;", //
				"method with name 'notExists' not found in String");
	}

	@Test
	public void testMethodReferenceMethod() {
		assertSuccess("interface I{void get();} class C{void m(I i){i.get();}} class D{void d(){}} new C().m(new D()::d);");
		assertSuccess("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} class D{int d(int x,int y){return x+y;}} assert new C().m(new D()::d,1,2)==3;");
		assertSuccess("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} class D{String d(String s, int... x){return s+x.length;}} assert new C().m(new D()::d).equals(\"l=3\");");
		assertSuccess("interface I{int get(int x, String s);} class C{int m(I i){return i.get(2, \"abc\");}} class D{int d(long x, Object s){return (int)x + ((String)s).length();}} assert new C().m(new D()::d) == 5;");
		assertSuccess("interface A{void get(int x, int y);} class B{void g(long a, Integer... b){}} A a = new B()::g;");
		assertFailCompile("interface A{void get(int x, int... y);} class B{void g(long a, Integer b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get(int x, int[] y)' of functional interface 'A'");
		assertSuccess("interface A{void get(int x, Integer y);} class B{void g(long a, int... b){}} A a = new B()::g;");
		assertFailCompile("interface A{void get(long x, Integer y);} class B{void g(int a, int... b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get(long x, Integer y)' of functional interface 'A'");
		assertFailCompile("interface A{void get(int x, long y);} class B{void g(int a, int... b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get(int x, long y)' of functional interface 'A'");
		assertFailCompile("interface A{void get();} class B{void g(int a, int... b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get()' of functional interface 'A'");
		assertSuccess("interface A{void get(int[] x);} class B{void g(int... b){}} A a = new B()::g;");
		assertSuccess("interface A{void get(int[][] x);} class B{void g(int[]... b){}} A a = new B()::g;");
		assertFailCompile("interface A{void get(int[][][] x);} class B{void g(int[]... b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get(int[][][] x)' of functional interface 'A'");
		assertSuccess("interface A{void get(Integer[] x);} class B{void g(int[]... b){}} A a = new B()::g;");
		assertFailCompile("interface A{void get(int[] x);} class B{void g(Integer[]... b){}} A a = new B()::g;", //
				"method 'g' of class 'B' doesn't match to the method 'get(int[] x)' of functional interface 'A'");

		assertFailCompile("interface I{void get();} class C{void m(I i){i.get(1);}} class D{int d(){return 1;}} new C().m(new D()::d);", //
				"cannot resolve method 'get' in 'I'"); // return type not match
		assertFailCompile("interface I{void get(int x);} class C{void m(I i){i.get();}} class D{int d(int x){return x;}} new C().m(new D()::d);", //
				"cannot resolve method 'get' in 'I'"); // return type not match
		assertFailCompile("interface I{int get();} class C{int m(I i){return i.get();}} class D{void d(){}} new C().m(new D()::d);", //
				"incompatible return type 'void' of method D.d(); expected return type 'int'"); // return type not match
		assertFailCompile("interface I{String get(int x);} class C{String m(I i){return i.get();}} class D{void d(int x){int y = x + 1;}} new C().m(new D()::d);", //
				"cannot resolve method 'get' in 'I'"); // return type not match

		assertFailCompile("interface I{void get(Object s);} class C{void m(I i){i.get(null);}} class D{void d(String s){}} new C().m(new D()::d);", //
				"method 'd' of class 'D' doesn't match to the method 'get(Object s)' of functional interface 'I'"); // arguments not match
		assertFailCompile("interface I{void get(long x, String s);} class C{void m(I i){i.get(1, \"a\");}} class D{void d(int x, Object s){}} new C().m(new D()::d);", //
				"method 'd' of class 'D' doesn't match to the method 'get(long x, String s)' of functional interface 'I'"); // arguments not match
	}

	@Test
	public void testReturnType() {
		assertSuccess("interface A{int get();} A a = () -> {return 1;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {{return 1;}}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {if(true) return 1; else return 2;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {if(false) {} else return 1; return 0;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {switch(1){case 1: return 1; case 2: return 2; default: return 3;}}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {try{return 1;} catch(Exception e){return 2;}}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {try{} catch(Exception e){return 2;} return 1;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {try{} catch(Exception e){} finally{return 1;}}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {for(int x = 0; x < 1; x++) return 1; return 0;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {for(int x : new int[]{1}) return x; return 0;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {int x = 1; while(x == 1) return x++; return 0;}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {do{return 1;}while(false);}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {synchronized(new Object()){return 1;}}; assert a.get() == 1;");
		assertSuccess("interface A{int get();} A a = () -> {LABEL:return 1;}; assert a.get() == 1;");
	}
}