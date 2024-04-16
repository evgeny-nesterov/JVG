import org.junit.jupiter.api.Test;

public class TestGenerics extends HiTest {
	@Test
	public void testClasses() {
		assertSuccessSerialize("class A<O>{}");
		assertSuccessSerialize("class A<O1, O2, O3, O4, O5, O6>{}");
		assertSuccessSerialize("class A<O extends Number>{}");
		assertSuccessSerialize("class A<O super Integer>{}");
		assertSuccessSerialize("class A<O extends A>{}");
		assertSuccessSerialize("class A<O super A>{}");

		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<Integer> b = new B();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<Number> b = new B<>();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A b = new B<Integer>();");
		assertSuccessSerialize("class A<O>{O m(O x) {return x;}} class B extends A<Integer>{} assert new B().m(123) == 123;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B extends A<Integer>{} B b = new B(); b.set(123); assert b.get() == 123; assert b.get() instanceof Integer;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B<O extends Integer> extends A<O>{} B b = new B(); b.set(123); assert b.get().equals(123); assert b.get() instanceof Integer;");

		assertSuccessSerialize("class A<O1,O2>{O1 o1; O1 o2;} class A1 extends A<Long,String>{}  class B<O extends A<Long,String>> extends A<Boolean, O>{} class C extends B<A1>{} C c = new C(); c.o1 = true; c.o2 = new A1(); c.o2.o1 = 1L; c.o2.o2 = \"abc\";");

		// failures
		assertFailCompile("class A<>{}");
		assertFailCompile("class A<{}");
		assertFailCompile("class A<O,>{}");
		assertFailCompile("class A<O,O>{}");
		assertFailCompile("class A<,>{}");
		assertFailCompile("class A<1>{}");
		assertFailCompile("class A<true>{}");
		assertFailCompile("class A<\"O\">{}");
		assertFailCompile("class A<?>{}");

		assertFailCompile("class A<? extends Object>{}");
		assertFailCompile("class A<? super Object>{}");
		assertFailCompile("class A<O extends>{}");
		assertFailCompile("class A<O extends O>{}");
		assertFailCompile("class A<O extends X>{}");
		assertFailCompile("class A<O extends 1>{}");
		assertFailCompile("class A<O extends true>{}");
		assertFailCompile("class A<O extends \"\">{}");
		assertFailCompile("class A<O extends int>{}");
		assertFailCompile("class A<O extends null>{}");
		assertFailCompile("class A<O extends boolean>{}");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class A{<O> void m() {}}");
		assertSuccessSerialize("class A{<O> O m(O x) {return x;}} assert new A().m(123) == 123;");
	}
}
