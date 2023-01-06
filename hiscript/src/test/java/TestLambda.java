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
	public void testMethod() {
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{});");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} assert new C().m((x,y)->x+y,1,2)==3; assert new C().m((x,y)->{return x+y;},1,2)==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} assert new C().m((s,x)->s+x.length).equals(\"l=3\");");
		assertFailCompile("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{return 1;});");
		assertFailCompile("interface I{void get(int x);} class C{void m(I i){i.get(1);}} new C().m(x->x);");
		assertFailCompile("interface I{int get();} class C{int m(I i){return i.get();}} new C().m(()->{});");
		assertFailCompile("interface I{String get(int x);} class C{int m(I i){return i.get(1);}} new C().m(x->{int y = x + 1;});");
	}

	@Test
	public void testConstructor() {
		assertSuccessSerialize("interface I{void get();} class C{C(I i){i.get();}} new C(()->{});");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int x; C(I i,int x,int y){this.x=i.get(x,y);}} assert new C((x,y)->x+y,1,2).x==3; assert new C((x,y)->{return x+y;},1,2).x==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String s; C(I i){s=i.get(\"l=\",1,2,3);}} assert new C((s,x)->s+x.length).s.equals(\"l=3\");");
		assertFailCompile("interface I{void get();} class C{C(I i){i.get();}} new C(()->{return 1;});");
		assertFailCompile("interface I{void get(int x);} class C{C(I i){i.get(1);}} new C(x->x);");
		assertFailCompile("interface I{int get();} class C{C(I i){return i.get();}} new C(()->{});");
		assertFailCompile("interface I{String get(int x);} class C{C(I i){return i.get(1);}} new C(x->{int y = x + 1;});");
	}

	@Test
	public void testArray() {
		assertSuccessSerialize("interface A{int get(int x);} A[][] a = {{x->x+1}}; assert a[0][0].get(1) == 2;");
		assertSuccessSerialize("interface A{int get(int x);} A[][] a = {{(byte x)->x+1}}; assert a[0][0].get(1) == 2;");
		assertFailCompile("interface A{int get(int x);} A[][] a = {{x->x==0}};");
		assertFailCompile("interface A{int get(int x, int y);} A[][][] a = {{{x->0}}};");
	}
}