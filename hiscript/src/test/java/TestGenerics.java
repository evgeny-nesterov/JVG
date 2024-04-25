import org.junit.jupiter.api.Test;

public class TestGenerics extends HiTest {
	@Test
	public void testClassesExtends() {
		// simple
		assertSuccessSerialize("class A<O>{}");
		assertSuccessSerialize("class A<O1, O2, O3, O4, O5, O6>{}");
		assertSuccessSerialize("class A<O extends Number>{}");
		assertSuccessSerialize("class A<O extends A>{}");
		assertSuccessSerialize("class A<O extends A<O>>{}");
		assertSuccessSerialize("class A<O extends A<?>>{}");
		assertSuccessSerialize("class A<O extends A<? extends A>>{}");
		assertSuccessSerialize("class A<O extends A<? extends Object>>{}");
		assertSuccessSerialize("class A<O extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A>>>>>>>>>>{}");

		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<Integer> b = new B();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A<? extends Number> b = new B();");
		assertSuccessSerialize("class A<O extends Number>{} class B extends A<Integer>{} A b = new B();");
		assertSuccessSerialize("class A<O>{O m(O x) {return x;}} class B extends A<Integer>{} assert new B().m(123) == 123;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B extends A<Integer>{} B b = new B(); b.set(123); assert b.get() == 123; assert b.get() instanceof Integer;");
		assertSuccessSerialize("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B<O extends Integer> extends A<O>{} B b = new B(); b.set(123); assert b.get().equals(123); assert b.get() instanceof Integer;");
		assertSuccessSerialize("class A<O extends Number>{O m(O o) {}} class B extends A<Integer>{Integer m(Integer o) {super.m(o);}}");
		assertFailCompile("class A<O extends Number>{O m(O o) {}} class B extends A<Boolean>{Boolean m(Boolean o) {super.m(o);}}");

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

		assertFailCompile("class A<O extends A<O extends A>>{}");
		assertFailCompile("class A<O1 extends A<O2 extends A>>{}");
		assertFailCompile("class A<O extends A<? extends String>>{}");
		assertFailCompile("class A<O extends A<Object, Object>>{}");
		assertFailCompile("class A<O1 extends A<Object>, O2>{}");
		assertFailCompile("class A<O1, O2>{} class B extends A<Object>{}");
		assertFailCompile("class A<O>{} class B extends A<Object, Object>{}");
		assertFailCompile("class A<O>{} class B extends A<>{}");
		assertFailCompile("class A<O extends Number>{} class B<O extends A> extends A<O>{}");
	}

	@Test
	public void testMethods() {
		assertSuccessSerialize("class A{<O> void m() {}}");
		assertSuccessSerialize("class A{<O> O m(O x) {return x;}} assert new A().m(123) == 123;");
		assertSuccessSerialize("class A<O>{O value; A(O value){this.value = value;} O get(){return value;}} assert new A<Boolean>(true).get();");
		assertSuccessSerialize("class A<O extends Number>{O x; O m(O x){this.x = x; return x;}} assert new A<Long>().m(1L) == 1;");
		assertSuccessSerialize("class A{<O extends Number> O m(O o) {}} class B extends A{Integer m(Integer o) {super.m(o);}}");

		//assertFailCompile("class A{<O> void m(? extends O x){}}"); // Wildcards may be used only as reference parameters
		assertFailCompile("class A{void m(O extends Number x){}}");
		assertFailCompile("class A{<O> void m(O extends Number x){}}");
	}

	@Test
	public void testFields() {
		assertSuccessSerialize("class A<O>{O value; A(O value){this.value = value;}} assert new A<Boolean>(true).value; assert new A<Integer>(123).value == 123;");

		assertFailCompile("class A{O extends Object x;}");
		// assertFailCompile("class A{? extends Object x;}");
	}

	@Test
	public void testInitialization() {
		assertSuccessSerialize("class A<O>{} A<String> a = new A<>();");
		assertSuccessSerialize("class A<O>{} A<String> a = new A<String>();");
		assertSuccessSerialize("class A<O>{} A<? extends Number> a = new A<Integer>();");
		assertSuccessSerialize("class A<O>{} A a = new A();");
		assertSuccessSerialize("class A<O>{} A<?> a = new A<String>();");
		assertFailCompile("class A<O>{} A<Object> a = new A<String>();");
		assertFailCompile("class A<O>{} A<String> a = new A<Integer>();");
		assertSuccessSerialize("class A<O extends Number>{}; A<Integer> a;}");

		assertFailCompile("class A{} A a = new A<>();");
		assertFailCompile("class A{} A a = new A<?>();");
		assertFailCompile("class A{} A a = new A<Integer>();");

		assertFailCompile("class A{} A<> a;");
		assertFailCompile("class A{} A<?> a;");
		assertFailCompile("class A{} A<String> a;");

		assertFailCompile("class A<O>{} A<> a;");
		assertFailCompile("class A<O>{} A<int> a;");
		assertFailCompile("class A<O>{} A<boolean> a;");
		assertFailCompile("class A<O>{} A<void> a;");
		assertFailCompile("class A<O extends Number>{}; A<Boolean> a;}");

		assertFailCompile("Object<Object> x;");
		assertFailCompile("Object<> x;");
		assertFailCompile("Object<?> x;");

		// 1 generic
		assertSuccessSerialize("class A<O extends Number>{} A<Integer> a = new A<>();");
		assertSuccessSerialize("class A<O extends Number>{} A<Integer> a = new A<Integer>();");
		assertSuccessSerialize("class A<O extends Number>{} A<?> a = new A<Integer>();");
		assertSuccessSerialize("class A<O extends Number>{} A a = new A<Integer>();");
		assertSuccessSerialize("class A<O extends Number>{} A<Integer> a = new A();");
		assertSuccessSerialize("class A<O extends Number>{} A<Integer> a = new A<>();");
		assertFailCompile("class A<O extends Number>{} A<Integer> a = new A<?>();"); // Wildcard type '?' cannot be instantiated directly
		assertFailCompile("class A<O extends Number>{} A<String> a;");
		assertFailCompile("class A<O extends Number>{} A a = new A<String>();");
		assertSuccessSerialize("class A<O>{} class B<O extends A<? extends Number>>{}");
		assertFailCompile("class A<O extends Number>{} class B<O extends A<? extends String>>{}");

		assertSuccessSerialize("class A<O extends A<?>>{} class B<O extends A<? extends A>>{}");
		assertFailCompile("class A<O extends A<?>>{} class B<O extends A<? extends Number>>{}");

		assertSuccessSerialize("class A<O extends A<?>>{} class A1 <O2 extends Number> extends A{} class B<O extends A<? extends A<? extends A1<Integer>>>>{}");
	}

	@Test
	public void testAnonymousClasses() {
		assertSuccessSerialize("interface A<O>{O get();} assert new A<Integer>(){Integer get(){return 123;}}.get() == 123;");
	}

	@Test
	public void testMethodInvocation() {
		assertSuccessSerialize("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; A<Integer> a = new A<>(); a.set(1); assert a.get() == 1;");
		//assertSuccessSerialize("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; class B<O extends Number> extends A<O>{} B<Integer> b = new B<>(); b.set(1); assert b.get() == 1;");

		// HashMap
		assertSuccessSerialize("HashMap<String, Integer> map = new HashMap<>(); map.put(\"a\", 1); assert map.get(\"a\") == 1;");
		assertSuccessSerialize("HashMap<String, Object> map = new HashMap<>(); map.put(\"a\", 1); map.put(\"b\", true); assert map.get(\"a\").equals(new Integer(1)); assert map.get(\"b\").equals(Boolean.TRUE);");

		// ArrayList
		assertSuccessSerialize("ArrayList<Number> list = new ArrayList<>(); list.add(1); list.add(1.23); assert list.get(0).equals(new Integer(1)); assert list.get(0) instanceof Integer; assert list.get(1).equals(new Double(1.23)); assert list.get(1) instanceof Double;");
		assertSuccessSerialize("ArrayList<Integer> list1 = new ArrayList<>(); list1.add(1); ArrayList<ArrayList<Integer>> list2 = new ArrayList<>(); list2.add(list1); assert list2.get(0).get(0) == 1;");
	}
}
