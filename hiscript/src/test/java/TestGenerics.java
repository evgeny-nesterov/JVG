import org.junit.jupiter.api.Test;

public class TestGenerics extends HiTest {
	@Test
	public void testClassesExtends() {
		// simple
		assertSuccessSerialize("class A<O>{}");
		assertSuccessSerialize("class A<O1, O2, O3, O4, O5, O6>{}");
		assertSuccessSerialize("class A<O extends Number>{}");
		assertSuccessSerialize("class A<O extends A>{}");

		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<Integer> b = new B();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<Number> b = new B<>();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A b = new B<Integer>();");
		assertSuccessSerialize("class A<O>{O m(O x) {return x;}} class B extends A<Integer>{} assert new B().m(123) == 123;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B extends A<Integer>{} B b = new B(); b.set(123); assert b.get() == 123; assert b.get() instanceof Integer;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B<O extends Integer> extends A<O>{} B b = new B(); b.set(123); assert b.get().equals(123); assert b.get() instanceof Integer;");

		assertSuccessSerialize("class A<O1,O2>{O1 o1; O2 o2;} class A1 extends A<Long,String>{}  class B<O extends A<Long,String>> extends A<Boolean, O>{} class C extends B<A1>{} " + //
				"B<A<Long,String>> c = new C(); c.o1 = true; c.o2 = new A1(); c.o2.o1 = 1L; c.o2.o2 = \"abc\"; " + //
				"assert c.o1; assert c.o2 instanceof A1; assert c.o2.o1 == 1L; assert c.o2.o2.equals(\"abc\");");
		assertSuccessSerialize("class A<O extends HashMap<O, O>>{}");
		assertSuccessSerialize("class A<O extends ArrayList<O>>{}");
		assertSuccessSerialize("class A<X extends Y, Y extends Z, Z extends Object> {}");
		assertSuccessSerialize("class A<X, Y> {} class B<X extends A<X, A<A, X>>> extends A<X, A<X, A>> {}");
		assertSuccessSerialize("class A<O1 extends HashMap<O2, O2>, O2 extends HashMap<O1, O1>>{}");
		assertSuccessSerialize("class A<O1 extends A<O2, O2>, O2 extends A<O1, O1>>{}");
		assertSuccessSerialize("class A <O extends Number>{O x; A(O x){this.x = x;} O getX(){return x;}} class B extends A<Integer>{B(Integer x){super(x);}} assert new B(1).x == 1; assert new B(2).getX() == 2;");

		// format failures
		assertFailCompile("class A<>{}");
		assertFailCompile("class A<{}");
		assertFailCompile("class A<O,>{}");
		assertFailCompile("class A<O,O>{}");
		assertFailCompile("class A<,>{}");
		assertFailCompile("class A<1>{}");
		assertFailCompile("class A<true>{}");
		assertFailCompile("class A<\"O\">{}");
		assertFailCompile("class A<?>{}");

		// structure failure
		assertFailCompile("class A<? extends Object>{}");
		assertFailCompile("class A<O extends>{}");
		assertFailCompile("class A<O extends X>{}");
		assertFailCompile("class A<O extends 1>{}");
		assertFailCompile("class A<O extends true>{}");
		assertFailCompile("class A<O extends \"\">{}");
		assertFailCompile("class A<O extends int>{}");
		assertFailCompile("class A<O extends null>{}");
		assertFailCompile("class A<O extends boolean>{}");
		assertFailCompile("class A<O super Integer>{}");
		assertFailCompile("class A<O super A>{}");

		// cyclic failure
		assertFailCompile("class A<O extends O>{}");
		assertFailCompile("class A<O, X extends Y, Y extends Z, Z extends X>{}");
		assertFailCompile("class A{<X extends Y, Y extends Z, Z extends X> X m(){}}");

		// extends failure
		assertFailCompile("class A <O extends Number>{} class B extends A<String>{}");
		assertFailCompile("class A{} class B<O>{} class C<X extends A>{} class D<Z extends B> extends C<Z>{}");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class A{<O> void m() {}}");
		assertSuccessSerialize("class A{<O> O m(O x) {return x;}} assert new A().m(123) == 123;");
	}
}
