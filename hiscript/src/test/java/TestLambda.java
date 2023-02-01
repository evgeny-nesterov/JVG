import org.junit.jupiter.api.Test;

public class TestLambda extends HiTest {
	@Test
	public void testField() {
		assertSuccessSerialize("interface I{void get();} I o = () -> {}; o.get();");
		assertSuccessSerialize("interface I{int get(int x);} I o = x -> x + 1; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x);} I o = (x) -> {x++; return x;}; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x);} I o = (int x) -> {return x + 1;}; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x, int y);} I o = (x, y) -> x + y; assert o.get(1, 2) == 3;");
		assertSuccessSerialize("interface I{int get(int x, int y);} I o = (byte x, short y) -> x + y; assert o.get(1, 2) == 3;");
		assertFailCompile("interface I{int get(int x, int y);} I o = (int x, y) -> 0;");
		assertFailCompile("interface I{int get(int x, int y);} I o = (x, int y) -> 0;");
		assertSuccessSerialize("interface I{String get(long x, String y);} I o = (x, y) -> x + y; assert o.get((byte)1, \"_\").equals(\"1_\");");
		assertFailCompile("Object o = (x) -> x;");
		assertFailCompile("interface I{int get(int x);} I o = (x, y) -> x + y;");
		assertFailCompile("interface I{int get(int x);} I o = (boolean x) -> x ? 1 : 0;");
		assertFailSerialize("interface I{int get(int x, int y);} I o = (x, y) -> x / y; o.get(1, 0);");
		// TODO assertFailCompile("int x = 0; interface I{int get(int x);} I o = x -> x;");
		assertFailCompile("interface I{void get();} I o = () -> 1;");
		assertSuccessSerialize("class C{int c = 0;} C c = new C(); interface I{void m(I i, int x);} I o = (i,x)->{if(x>0) i.m(i, x-1); c.c++;}; o.m(o, 5); assert c.c == 6;");
		assertSuccessSerialize("interface I{int get(int a);} interface J{int sum(int a, I... i);} J j = (a,i) -> {int c = 0; for(int x=0;x<i.length;x++) c+=i[x].get(a); return c;} assert j.sum(3, a->a, a->1, a->a/2) == 5;");
	}

	@Test
	public void testMethodArgument() {
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{});");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} assert new C().m((x,y)->x+y,1,2)==3; assert new C().m((x,y)->{return x+y;},1,2)==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} assert new C().m((s,x)->s+x.length).equals(\"l=3\");");
		assertFailCompile("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{return 1;});");
		assertFailCompile("interface I{void get(int x);} class C{void m(I i){i.get(1);}} new C().m(x->x);");
		assertFailCompile("interface I{int get();} class C{int m(I i){return i.get();}} new C().m(()->{});");
		assertFailCompile("interface I{String get(int x);} class C{String m(I i){return i.get(1);}} new C().m(x->{int y = x + 1;});");
	}

	@Test
	public void testConstructorArgument() {
		assertSuccessSerialize("interface I{void get();} class C{C(I i){i.get();}} new C(()->{});");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int x; C(I i,int x,int y){this.x=i.get(x,y);}} assert new C((x,y)->x+y,1,2).x==3; assert new C((x,y)->{return x+y;},1,2).x==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String s; C(I i){s=i.get(\"l=\",1,2,3);}} assert new C((s,x)->s+x.length).s.equals(\"l=3\");");
		assertFailCompile("interface I{void get();} class C{C(I i){i.get();}} new C(()->{return 1;});");
		assertFailCompile("interface I{void get(int x);} class C{C(I i){i.get(1);}} new C(x->x);");
		assertFailCompile("interface I{int get();} class C{C(I i){i.get();}} new C(()->{});");
		assertFailCompile("interface I{String get(int x);} class C{C(I i){i.get(1);}} new C(x->{int y = x + 1;});");
	}

	@Test
	public void testArray() {
		assertSuccessSerialize("interface A{int get(int x);} A[][] a = {{x->x+1}}; assert a[0][0].get(1) == 2;");
		assertSuccessSerialize("interface A{int get(int x);} A[][] a = {{(byte x)->x+1}}; assert a[0][0].get(1) == 2;");
		assertFailCompile("interface A{int get(int x);} A[][] a = {{x->x==0}};");
		assertFailCompile("interface A{int get(int x, int y);} A[][][] a = {{{x->0}}};");
	}

	@Test
	public void testMethodReferenceDeclaration() {
		// string
		assertSuccessSerialize("interface A{int length();} A a = \"abc\"::length; assert a.length() == 3;"); // same method name
		assertSuccessSerialize("interface A{int getLength();} A a = \"abc\"::length; assert a.getLength() == 3;"); // another method name
		assertFailCompile("interface A{int getLength();} A a = \"abc\"::length; assert a.length() == 3;"); // method name is not match
		assertFailCompile("interface A{int length(); int size();} A a = \"abc\"::length;"); // not a functional interface

		// object
		assertSuccessSerialize("interface A{void get();} class C{void get(){}}; C c = new C(); A a = c::get; a.get();");
		assertSuccessSerialize("interface A{void get(); default void get2(){} static void get3(){}} class C{void get(){}}; C c = new C(); A a = c::get; a.get();");
		assertSuccessSerialize("interface A{void get();} class C{static void get(){}}; A a = C::get; a.get();");
		assertFailCompile("interface A{void get(); void get2();} class C{void m(){}}; A a = new C()::m;");
		assertFailCompile("abstract class A{abstract void get();} class C{void get(){}}; A a = new C()::get;");
		assertSuccessSerialize("interface A{void get();} class C{void get(){assert false;} void m(){}}; A a = new C()::m;");
		assertSuccessSerialize("interface A{void get();} class C{static void get(){} static void m(){}}; A a = C::m;"); // static method

		assertSuccessSerialize("interface A1{void get();} interface A2{void get(int x);} class C{void get(){} void get(int x){}}; C c = new C(); A1 a1 = c::get; a1.get(); A2 a2 = c::get; a2.get(1);");
		assertSuccessSerialize("interface A1{void get();} interface A2{void get(int x);} class C{static void get(){} static void get(int x){}}; A1 a1 = C::get; a1.get(); A2 a2 = C::get; a2.get(1);");

		assertSuccessSerialize("interface A{int get(int x);} class C{int c = 1; int get(int x){return c + x + 1;}}; A a = new C()::get; assert a.get(1) == 3;");
		assertSuccessSerialize("interface A{int get(int x);} class C{static int c = 1; static int get(int x){return c + x + 1;}}; A a = C::get; assert a.get(1) == 3;");

		assertSuccessSerialize("interface A1{int get();} interface A2{int get(int x);} class C{int get(){return 1;} int get(int x){return x;}}; C c = new C(); A1 a1 = c::get; assert a1.get() == 1; A2 a2 = c::get; assert a2.get(2) == 2;");
		assertSuccessSerialize("interface A1{int get();} interface A2{int get(int x);} class C{static int get(){return 1;} static int get(int x){return x;}}; A1 a1 = C::get; assert a1.get() == 1; A2 a2 = C::get; assert a2.get(2) == 2;");

		// extends
		assertSuccessSerialize("interface A{int get();} interface B extends A{} abstract class C{int get(){return 1;}}; class D extends C{}; D d = new D(); B b = d::get; assert b.get() == 1;");
		assertSuccessSerialize("interface A{int get();} interface B extends A{} abstract class C{abstract int get();}; class D extends C{int get(){return 1;}}; D d = new D(); B b = d::get; assert b.get() == 1;");
		assertSuccessSerialize("interface A{int get();} interface B extends A{} class C implements A{int get(){return 1;}}; C c = new C(); B b = c::get; assert b.get() == 1;");
		assertSuccessSerialize("interface A{int get(); int get2();} interface B extends A{default int get2(){return 2;}} class C{int get1(){return 1;}}; B b = new C()::get1; assert b.get() == 1;");
	}

	@Test
	public void testMethodReferenceMethod() {
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} class D{void d(){}} new C().m(new D()::d);");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} class D{int d(int x,int y){return x+y;}} assert new C().m(new D()::d,1,2)==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} class D{String d(String s, int... x){return s+x.length;}} assert new C().m(new D()::d).equals(\"l=3\");");
		assertSuccessSerialize("interface I{int get(int x, String s);} class C{int m(I i){return i.get(2, \"abc\");}} class D{int d(long x, Object s){return (int)x + ((String)s).length();}} assert new C().m(new D()::d) == 5;");

		assertFailCompile("interface I{void get();} class C{void m(I i){i.get(1);}} class D{int d(){return 1;}} new C().m(new D()::d);"); // return type not match
		assertFailCompile("interface I{void get(int x);} class C{void m(I i){i.get();}} class D{int d(int x){return x;}} new C().m(new D()::d);"); // return type not match
		assertFailCompile("interface I{int get();} class C{int m(I i){return i.get();}} class D{void d(){}} new C().m(new D()::d);"); // return type not match
		assertFailCompile("interface I{String get(int x);} class C{String m(I i){return i.get();}} class D{void d(int x){int y = x + 1;}} new C().m(new D()::d);"); // return type not match

		assertFailCompile("interface I{void get(Object s);} class C{void m(I i){i.get(null);}} class D{void d(String s){}} new C().m(new D()::d);"); // arguments not match
		assertFailCompile("interface I{void get(long x, String s);} class C{void m(I i){i.get(1, \"a\");}} class D{void d(int x, Object s){}} new C().m(new D()::d);"); // arguments not match
	}
}