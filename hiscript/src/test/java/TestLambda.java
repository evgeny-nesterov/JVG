import org.junit.jupiter.api.Test;

public class TestLambda extends HiTest {
	@Test
	public void test() {
		// field
		assertSuccessSerialize("interface I{void get();} I o = () -> {}; o.get();");
		assertSuccessSerialize("interface I{int get(int x);} I o = x -> x + 1; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x);} I o = (x) -> {x++; return x;}; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x);} I o = (int x) -> {return x + 1;}; assert o.get(1) == 2;");
		assertSuccessSerialize("interface I{int get(int x, int y);} I o = (x, y) -> x + y; assert o.get(1, 2) == 3;");
		assertSuccessSerialize("interface I{String get(long x, String y);} I o = (x, y) -> x + y; assert o.get((byte)1, \"_\").equals(\"1_\");");
		assertFailCompile("Object o = (x) -> x;");
		assertFailCompile("interface I{int get(int x);} I o = (x, y) -> x + y;");
		assertFailCompile("interface I{int get(int x);} I o = (boolean x) -> x ? 1 : 0;");
		assertFailSerialize("interface I{int get(int x, int y);} I o = (x, y) -> x / y; o.get(1, 0);");
		// TODO assertFailCompile("int x = 0; interface I{int get(int x);} I o = x -> x;");

		// method
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{});");
		assertSuccessSerialize("interface I{int get(int x,int y);} class C{int m(I i,int x,int y){return i.get(x,y);}} assert new C().m((x,y)->x+y,1,2)==3; assert new C().m((x,y)->{return x+y;},1,2)==3;");
		assertSuccessSerialize("interface I{String get(String s, int... x);} class C{String m(I i){i.get(\"l=\",1,2,3);}} assert new C().m((s,x)->s+x.length).equals(\"l=3\");");
	}
}