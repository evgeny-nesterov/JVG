import org.junit.jupiter.api.Test;

public class TestGenerics extends HiTest {
	@Test
	public void testClassesExtends() {
		// simple
		assertSuccess("class A<O>{}");
		assertSuccess("class A<O1, O2, O3, O4, O5, O6>{}");
		assertSuccess("class A<O extends Number>{}");
		assertSuccess("class A<O extends A>{}");
		assertSuccess("class A<O extends A<O>>{}");
		assertSuccess("class A<O extends A<?>>{}");
		assertSuccess("class A<O extends A<? extends A>>{}");
		assertSuccess("class A<O extends A<? extends Object>>{}");
		assertSuccess("class A<O extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A<? extends A>>>>>>>>>>{}");

		assertSuccess("class A<O extends Number>{} class B extends A<Integer>{} A<Integer> b = new B();");
		assertSuccess("class A<O extends Number>{} class B extends A<Integer>{} A<? extends Number> b = new B();");
		assertSuccess("class A<O extends Number>{} class B extends A<Integer>{} A b = new B();");
		assertSuccess("class A<O>{O m(O x) {return x;}} class B extends A<Integer>{} assert new B().m(123) == 123;");
		assertSuccess("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B extends A<Integer>{} B b = new B(); b.set(123); assert b.get() == 123; assert b.get() instanceof Integer;");
		assertSuccess("class A<O extends Number>{O value; O get(){return value;} void set(O value){this.value = value;}} class B<O extends Integer> extends A<O>{} B b = new B(); b.set(123); assert b.get().equals(123); assert b.get() instanceof Integer;");
		assertSuccess("class A<O extends Number>{O m(O o) {}} class B extends A<Integer>{Integer m(Integer o) {super.m(o);}}");
		assertFailCompile("class A<O extends Number>{O m(O o) {}} class B extends A<Boolean>{Boolean m(Boolean o) {super.m(o);}}");

		assertSuccess("class A<O1,O2>{O1 o1; O2 o2;} class A1 extends A<Long,String>{}  class B<O extends A<Long,String>> extends A<Boolean, O>{} class C extends B<A1>{} " + //
				"B<A<Long,String>> c = new C(); c.o1 = true; c.o2 = new A1(); c.o2.o1 = 1L; c.o2.o2 = \"abc\"; " + //
				"assert c.o1; assert c.o2 instanceof A1; assert c.o2.o1 == 1L; assert c.o2.o2.equals(\"abc\");");
		assertSuccess("class A<O extends HashMap<O, O>>{}");
		assertSuccess("class A<O extends ArrayList<O>>{}");
		assertSuccess("class A<X extends Y, Y extends Z, Z extends Object> {}");
		assertSuccess("class A<X, Y> {} class B<X extends A<X, A<A, X>>> extends A<X, A<X, A>> {}");
		assertSuccess("class A<O1 extends HashMap<O2, O2>, O2 extends HashMap<O1, O1>>{}");
		assertSuccess("class A<O1 extends A<O2, O2>, O2 extends A<O1, O1>>{}");
		assertSuccess("class A <O extends Number>{O x; A(O x){this.x = x;} O getX(){return x;}} class B extends A<Integer>{B(Integer x){super(x);}} assert new B(1).x == 1; assert new B(2).getX() == 2;");

		assertSuccess("class A<X, Y>{} class B extends A{} new B();");
		assertSuccess("class A<X extends Number, Y extends A>{} class B<Y extends A, X extends Integer> extends A<X, Y>{} new B<A, Integer>();");
		assertSuccess("class A<O>{} new A<A<A<A>>>();");
		assertFailCompile("class A<O>{} new A<Object<Object>>();");
		assertSuccess("class A<O>{O x;} class B extends A<Integer>{int get(){x = 1; return x;}} assert new B().get() == 1;");
		assertSuccess("class A<O1, O2 extends O1, O3 extends O2>{O1 o1; O2 o2; O3 o3;} A<Number, Integer, Integer> a = new A<>(); a.o1 = 1.0; a.o2 = 2; a.o3 = new Integer(3);");

		// format failures
		assertFailCompile("class A<>{}");
		assertFailCompile("class A<{}");
		assertFailCompile("class A<O,>{}");
		assertFailCompile("class A<O,O>{}");
		assertFailCompile("class A<,>{}");
		assertFailCompile("class A<1>{}");
		assertFailCompile("class A<true>{}");
		assertFailCompile("class A<\"O\">{}");

		// structure failure
		assertFailCompile("class A<?>{}");
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
		assertFailCompile("class A{} class B extends A<Object>{}");
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
		assertFailCompile("class A<O>{} class B extends A<Object, Object>{}");
	}

	@Test
	public void testMethods() {
		assertSuccess("class A{<O> void m() {}}");
		assertSuccess("class A{<O> O m(O x) {return x;}} assert new A().m(123) == 123;");
		assertSuccess("class A<O>{O value; A(O value){this.value = value;} O get(){return value;}} assert new A<Boolean>(true).get();");
		assertSuccess("class A<O extends Number>{O x; O m(O x){this.x = x; return x;}} assert new A<Long>().m(1L) == 1;");
		assertSuccess("class A{<O extends Number> O m(O o) {return o;}} class B extends A{Integer m(Integer o) {return super.m(o);}}");

		//assertFailCompile("class A{<O> void m(? extends O x){}}"); // Wildcards may be used only as reference parameters
		assertFailCompile("class A{void m(O extends Number x){}}");
		assertFailCompile("class A{<O> void m(O extends Number x){}}");
		assertFailCompile("class A<O>{void m(O extends Number x){}}");
		assertFailCompile("class A{<> void m(){}}");
		assertFailCompile("class A{<?> void m(){}}");
		assertFailCompile("class A{<? extends Object> void m(){}}");
		assertFailCompile("class A{<? super Object> void m(){}}");
		assertFailCompile("class X<O>{} class A{void m(){X<> x}}");
		assertFailCompile("class X<O>{} class A{void m(){X<int> x}}");
		assertFailCompile("class X<O>{} class A{<X extends int> void m(){}}");
		assertFailCompile("class A{void m(? x);}");
		assertFailCompile("class A{void m(? extends Object x);}");
		assertFailCompile("class A{<O> void m(? extends O x);}");
	}

	@Test
	public void testConstructors() {
		assertSuccess("class A{<O> A(O o) {}} new A(1); new A(true);");
		assertSuccess("class A<O>{} class B{<O extends Number> B(A<O> a){}} new B(new A<Integer>());");
		assertSuccess("class A<O>{O value; A(O value){this.value=value;} O get(){return value;}} class B<O extends Number> extends A<O>{B(O value){super(value);}} B<Integer> b = new B<>(2); assert b.get() == 2;");
		assertSuccess("class A<O>{O value; A(O value){this.value=value;} O get(){return value;}} class B<O extends Number> extends A<O>{B(O value){super(value);}} assert new B<Integer>(2).get() == 2;");
		assertSuccess("class A<O1>{O1 value; A(O1 value){this.value=value;} O1 get(){return value;}} class B<O2 extends Number> extends A<O2>{B(O2 value){super(value);}} B<Integer> b = new B<>(2); assert b.get() == 2;");

		assertFailCompile("class A{A(O extends Number x){}}");
		assertFailCompile("class A{<O> A(O extends Number x){}}");
		assertFailCompile("class A{<> A(){}}");
		assertFailCompile("class A{<?> A(){}}");
		assertFailCompile("class A{<? extends Object> A(){}}");
		assertFailCompile("class A{<? super Object> A(){}}");
		assertFailCompile("class X<O>{} class A{A(){X<> x}}");
		assertFailCompile("class X<O>{} class A{A(){X<int> x}}");
		assertFailCompile("class X<O>{} class A{<X extends int> A(){}}");
	}

	@Test
	public void testFields() {
		assertSuccess("class A<O>{O vvv; A(O vvv){this.vvv = vvv;}} assert new A<Boolean>(true).vvv; assert new A<Integer>(123).vvv == 123;");
		assertSuccess("class A<O1 extends Number>{O1 x;} class B<O2 extends Integer> extends A<O2>{} new B<Integer>().x = new Integer(1);");
		assertSuccess("class A<O extends Number>{O x;} class B<O extends Integer> extends A<O>{} B<Integer> b = new B<>(); b.x = new Integer(1); assert b.x == 1; assert b.x instanceof Integer;");
		assertSuccess("class A<O>{O m(O x) {O y = x; return y;}} assert new A<Integer>().m(1) == 1;");
		assertSuccess("class A<O>{O m(O x) {O y = x != null ? x : null; return y;}} assert new A<Boolean>().m(true);");

		assertFailCompile("class A{O extends Object x;}");
		assertFailCompile("class A{? extends Object x;}");
		assertFailCompile("class A{void m(){? extends Object x;}}");
		assertFailCompile("class A{A(){? extends Object x;}}");
		assertFailCompile("class A{{? extends Object x;}}");
	}

	@Test
	public void testInitialization() {
		assertSuccess("class A<O>{} A<String> a = new A<>();");
		assertSuccess("class A<O>{} A<String> a = new A<String>();");
		assertSuccess("class A<O>{} A<? extends Number> a = new A<Integer>();");
		assertSuccess("class A<O>{} A a = new A();");
		assertSuccess("class A<O>{} A<?> a = new A<String>();");
		assertFailCompile("class A<O>{} A<Object> a = new A<String>();");
		assertFailCompile("class A<O>{} A<String> a = new A<Integer>();");
		assertSuccess("class A<O extends Number>{}; A<Integer> a;}");

		assertFailCompile("class A{} A a = new A<>();");
		assertFailCompile("class A{} A a = new A<?>();");
		assertFailCompile("class A{} A a = new A<Integer>();");
		assertFailCompile("class A{} A a = new A<Object, Object>();");

		assertFailCompile("class A{} A<> a;");
		assertFailCompile("class A{} A<?> a;");
		assertFailCompile("class A{} A<String> a;");

		assertFailCompile("class A<O>{} A<> a;");
		assertFailCompile("class A<O>{} A<int> a;");
		assertFailCompile("class A<O>{} A<boolean> a;");
		assertFailCompile("class A<O>{} A<void> a;");
		assertFailCompile("class A<O extends Number>{}; A<Boolean> a;}");
		assertFailCompile("class A<O>{} A a = new A<Object, Object>();");

		assertFailCompile("Object<Object> x;");
		assertFailCompile("Object<> x;");
		assertFailCompile("Object<?> x;");

		// 1 generic
		assertSuccess("class A<O extends Number>{} A<Integer> a = new A<>();");
		assertSuccess("class A<O extends Number>{} A<Integer> a = new A<Integer>();");
		assertSuccess("class A<O extends Number>{} A<?> a = new A<Integer>();");
		assertSuccess("class A<O extends Number>{} A a = new A<Integer>();");
		assertSuccess("class A<O extends Number>{} A<Integer> a = new A();");
		assertSuccess("class A<O extends Number>{} A<Integer> a = new A<>();");
		assertFailCompile("class A<O extends Number>{} A<Integer> a = new A<?>();"); // Wildcard type '?' cannot be instantiated directly
		assertFailCompile("class A<O extends Number>{} A<String> a;");
		assertFailCompile("class A<O extends Number>{} A a = new A<String>();");
		assertSuccess("class A<O>{} class B<O extends A<? extends Number>>{}");
		assertFailCompile("class A<O extends Number>{} class B<O extends A<? extends String>>{}");

		assertSuccess("class A<O extends A<?>>{} class B<O extends A<? extends A>>{}");
		assertFailCompile("class A<O extends A<?>>{} class B<O extends A<? extends Number>>{}");

		assertSuccess("class A<O extends A<?>>{} class A1 <O2 extends Number> extends A{} class B<O extends A<? extends A<? extends A1<Integer>>>>{}");
	}

	@Test
	public void testAnonymousClasses() {
		assertSuccess("interface A<O>{O get();} assert new A<Byte>(){Byte get(){return 123;}}.get() == 123;");
		assertSuccess("interface A<O>{O get();} assert new A<Short>(){Short get(){return 123;}}.get() == 123;");
		assertSuccess("interface A<O>{O get();} assert new A<Character>(){Character get(){return '!';}}.get() == '!';");
		assertSuccess("interface A<O>{O get();} assert new A<Integer>(){Integer get(){return 123;}}.get() == 123;");
		assertSuccess("interface A<O>{O get();} assert new A<Long>(){Long get(){return 123L;}}.get() == 123L;");
		assertSuccess("interface A<O>{O get();} assert new A<Float>(){Float get(){return 1.23f;}}.get() == 1.23f;");
		assertSuccess("interface A<O>{O get();} assert new A<Double>(){Double get(){return 1.23;}}.get() == 1.23;");
		assertSuccess("interface A<O>{O get();} assert new A<Boolean>(){Boolean get(){return true;}}.get() == true;");

		assertSuccess("class A<O>{A(O a){}} new A<Integer>(0){};");
		assertSuccess("class A<O>{O a;} Integer i = new A<Integer>(){}.a; assert i == null;");
		assertSuccess("class A<O>{O a; A(O a){this.a = a;} O get(){return a;}} assert new A<Integer>(123){Integer get(){return a + 1;}}.get() == 124;");

		assertFailCompile("interface A<O>{O get();} assert new A<Boolean>(){Boolean get(){return true;}}.get() == 1;", //
				"operator '==' can not be applied to Boolean, int");
	}

	@Test
	public void testInnerClasses() {
//		TODO assertSuccessSerialize("class A<O>{O get(O v);} class B<O>{class C extends A<O>{O get(O v){return v;}}}");
		assertSuccess("interface A<O1>{} class B<O2>{class C implements A<O2>{}}");
	}

	@Test
	public void testMethodInvocation() {
		assertSuccess("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; A<Integer> a = new A<>(); a.set(1); assert a.get() == 1;");
		assertSuccess("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; class B<O extends Number> extends A<O>{} B<Integer> b = new B<>(); b.set(1); assert b.get() == 1;");
		assertSuccess("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; class B<O extends Number> extends A<O>{void set(O value){super.set(value);}} B<Integer> b = new B<>(); b.set(2); assert b.get() == 2;");
		assertSuccess("class A<O>{O value; A<O> set(O value){this.value=value; return this;} O get(){return value;}} class B<O extends Number> extends A<O>{A<O> set(O value){return super.set(value);}} assert new B<Integer>().set(2).get() == 2;");
		assertFailCompile("class A<O>{O value; A<O> set(O value){this.value=value; return this;} O get(){return value;}} class B<O extends Number> extends A<O>{B<O> set(O value){return super.set(value);}}", //
				"incompatible types; found A, required B");
		assertSuccess("class A<O1>{O1 value; void set(O1 value){this.value=value;} O1 get(){return value;}} class B<O2 extends Number> extends A<O2>{void set(O2 value){super.set(value);}} B<Integer> b = new B<>(); b.set(2); assert b.get() == 2;");

		// HashMap
		assertSuccess("HashMap<String, Integer> map = new HashMap<>(); map.put(\"a\", 1); assert map.get(\"a\") == 1;");
		assertSuccess("HashMap<String, Object> map = new HashMap<>(); map.put(\"a\", 1); map.put(\"b\", true); assert map.get(\"a\").equals(new Integer(1)); assert map.get(\"b\").equals(Boolean.TRUE);");
		assertSuccess("HashMap<?, ?> map = new HashMap<>(); map.put(\"a\", new Integer(1)); map.put(\"b\", Boolean.TRUE); assert map.get(\"a\").equals(new Integer(1)); assert map.get(\"b\").equals(Boolean.TRUE);");
		assertFailCompile("HashMap<?, ?> map = new HashMap<>(); map.put(\"a\", 1);");
		assertFailCompile("HashMap<?, ?> map = new HashMap<? extends Integer, ?>();");

		// ArrayList
		assertSuccess("ArrayList<Number> list = new ArrayList<>(); list.add(1); list.add(1.23); assert list.get(0).equals(new Integer(1)); assert list.get(0) instanceof Integer; assert list.get(1).equals(new Double(1.23)); assert list.get(1) instanceof Double;");
		assertSuccess("ArrayList<?> list = new ArrayList<>(); list.add(new Integer(1)); list.add(new Double(1.23)); assert list.get(0).equals(new Integer(1)); assert list.get(0) instanceof Integer; assert list.get(1).equals(new Double(1.23)); assert list.get(1) instanceof Double;");
		assertFailCompile("ArrayList<?> list = new ArrayList<>(); list.add(1);");
		assertFailCompile("ArrayList<? extends Number> list = new ArrayList<>(); list.add(1);");
		assertFailCompile("ArrayList<? extends Number> list = new ArrayList<>(); list.add(\"abc\");");
		assertFailCompile("ArrayList<?> list = new ArrayList<?>();");
		assertFailCompile("ArrayList<? extends Number> list = new ArrayList<? extends Number>();");
		assertSuccess("ArrayList<Integer> list1 = new ArrayList<>(); list1.add(1); ArrayList<ArrayList<Integer>> list2 = new ArrayList<>(); list2.add(list1); assert list2.get(0).get(0) == 1;");
	}

	@Test
	public void testSuper() {
		// super in field
		assertSuccess("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} " + //
				"A<C1> x_ = new A<>(); x_.m(new C1()); x_.m(new C2()); x_.m(new C3()); " + //
				"A<? super C1> x = new A<>(); x = new A<C1>(); x = x_; x.m(new C1()); x.m(new C2()); x.m(new C3());");
		assertSuccess("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} " + //
				"A<C1> x_ = new A<>(); x_.m(new C1()); x_.m(new C2()); x_.m(new C3()); " + //
				"A<? super C2> x = new A<>(); x = new A<C1>(); x = new A<C2>(); x = x_; x.m(new C2()); x.m(new C3());");
		assertSuccess("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} " + //
				"A<C1> x_ = new A<>(); x_.m(new C1()); x_.m(new C2()); x_.m(new C3()); " + //
				"A<? super C3> x = new A<>(); x = new A<C1>(); x = new A<C2>(); x = new A<C3>(); x = x_; x.m(new C3());");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C1> x = new A<C2>();");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C2> x = new A<C3>();");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C2> x = new A<C1>(); x.m(new C1());");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C3> x = new A<C1>(); x.m(new C1());");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C3> x = new A<C1>(); x.m(new C2());");
		assertFailCompile("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} A<? super C3> x = new A<String>();");

		assertSuccess("class C<O>{O o; C(O o){this.o=o;} O get(O o){return o;}} C<? super Integer> c = new C<Integer>(1); c.get(1);");
		assertSuccess("class C1{} class C2 extends C1{} HashMap<? super C2, ? super C2> map = new HashMap<C1, C1>();");

		// fails
		assertFailCompile("class A<O>{} A<? super Number> x = new A<Integer>();");
		assertFailCompile("class A<O extends Integer>{} A<? super Number> x;");
		assertFailCompile("class A<O super Integer>{}");
		assertFailCompile("class A{<O super Integer> void m(O x){}}");
		assertFailCompile("class A{<O super Integer> A(O o){}}");
		assertFailCompile("class A<O>{} class B extends A<>{}");

		assertFailCompile("class A<>{}");
		assertFailCompile("class A<O>{} class B{A<> void m(){}}");
		assertFailCompile("class A<O>{} class B{A<?,?> void m(){}}");
		assertFailCompile("class A<O>{} class B{A<int> void m(){}}");
		assertFailCompile("class A<O>{} class B{void m(A<> x){}}");
		assertFailCompile("class A<O>{} class B{void m(A<?,?> x){}}");
		assertFailCompile("class A<O>{} class B{void m(A<int> x){}}");
		assertFailCompile("class A<O>{} class B{B(A<> x){}}");
		assertFailCompile("class A<O>{} class B{B(A<?,?> x){}}");
		assertFailCompile("class A<O>{} class B{B(A<int> x){}}");
		assertFailCompile("class A<O>{} class B{A<> x;}");
		assertFailCompile("class A<O>{} class B{A<?,?> x;}");
		assertFailCompile("class A<O>{} class B{A<int> x;}");
	}

	@Test
	public void testArrays() {
		assertSuccess("class C<O>{} C<Integer>[] a = new C[1]; a[0] = new C<>(); a[0] = new C<Integer>();");
		assertSuccess("class C<O>{} C<? extends Number>[] a = new C[1]; a[0] = new C<>(); a[0] = new C<Integer>();");
		assertFailCompile("class C<O>{} C<Integer>[] a = new C<>[1];");
		assertFailCompile("class C<O>{} C<Integer>[] a = new C<Integer>[1];");
		assertFailCompile("class C<O>{} C<? extends Integer>[] a = new C[1]; a[0] = new C<Number>();");
	}

	@Test
	public void testAssignments() {
		assertSuccess("class C{<O> O get(){return (O)new Integer(1);}} Integer v = new C().get(); assert v == 1;");
		assertFailCompile("class C<O>{O get(){return (O)new Integer(1);}} Integer v = new C().get(); assert v == 1;");

		assertSuccess("class C{<O extends Number> O get(){return (O)new Double(1.23);}} Double v = new C().get(); assert v == 1.23;");
		assertFailCompile("class C<O extends Number>{O get(){return (O)new Double(1.23);}} Double v = new C().get(); assert v == 1.23;");

		assertSuccess("class C{<O extends Double> O get(){return (O)new Double(1.23);}} Number v = new C().get(); assert v.equals(new Double(1.23));");
		assertSuccess("class C<O extends Double>{O get(){return (O)new Double(1.23);}} Number v = new C().get(); assert v.equals(new Double(1.23));");

		assertFail("class C{<O> O get(){return (O)new Integer(1);}} Boolean v = new C().get();", //
				"cannot convert 'Integer' to 'Boolean'");
	}

	@Test
	public void testReturn() {
		assertSuccess("class C{<O extends Number> O get(){return (O)new Double(1.23);}}");
		assertFailCompile("class C{<O extends Number> O get(){return 1.23;}}");
		assertFailCompile("class C<O extends Number>{O get(){return 1.23;}}");
		assertFailCompile("class C<O extends Number>{O get(){return (O)1.23;}}");

		assertSuccess("class C{<O> O get(){return (O)\"abc\";}} String abc = new C().get(); assert abc.equals(\"abc\");");

		assertSuccess("class C{<O> O get(){return (O)\"abc\";}} String s = new C().get(); assert s.equals(\"abc\");");
		assertFailCompile("class C{<O> O get(){return \"abc\";}}");
		assertFail("class C{<O> O get(){return (O)\"abc\";}} Boolean s = new C().get();", //
				"cannot convert 'String' to 'Boolean'");

		assertSuccess("class C{<O> O get(O o){return o;}} C c = new C().get(new C()) assert c instanceof C;");
		assertSuccess("class C{<O extends Number> Number get(O o){return o;}} Number n = new C().get(1d); assert n.equals(new Double(1));");
		assertSuccess("class C{<O extends Number> O get(O o){return o;}} Float f = new C().get(1.23f); assert f == 1.23F;");
		assertSuccess("class C{<O1, O2> O1 get(O2 o2){return (O1)o2;}} String a = new C().get(\"a\"); assert a.equals(\"a\");");
		assertFailCompile("class C{<O1, O2> O1 get(O2 o2){return o2;}}");
		assertFailCompile("class C{<O1 extends Number, O2 extends Integer> O1 get(O2 o2){return o2;}}");

		assertSuccess("class A1{} class A2 extends A1{} interface I<O extends A1>{O get();} class B<O extends A2> implements I<O>{public O get(){return (O)new A2();}} assert new B<A2>().get() instanceof A2;");
		assertSuccess("class A1{} class A2 extends A1{} interface I<O1 extends A1>{O1 get();} class B<O2 extends A2> implements I<O2>{public O2 get(){return (O2)new A2();}} assert new B<A2>().get() instanceof A2;");

		assertFail("class C{<O> O get(){return (O)\"\";}} Integer i = new C().get();", //
				"cannot convert 'String' to 'Integer'");
	}

	@Test
	public void testArguments() {
		assertSuccess("class A<O>{O o; A(O o){this.o=o;} O get(){return o;}} class B{<O> O get(A<O> a){return a.get();}} A<Integer> a = new A<>(1); Integer v = new B().get(a); assert v == 1;");
		assertSuccess("class A<AAA>{AAA o; A(AAA o){this.o=o;} AAA get(){return o;}} class B{<AAA> AAA get(A<AAA> a){return a.get();}} A<Integer> a = new A<>(1); Integer v = new B().get(a); assert v == 1;");
		assertFailCompile("class A<AAA>{O o; A(AAA o){this.o=o;} O get(){return o;}} class B{<AAA> AAA get(A<AAA> a){return a.get();}} A<Integer> a = new A<>(1); Integer v = new B().get(a); assert v == 1;", //
				"class 'O' can not be resolved");
	}

	@Test
	public void testLambda() {
		assertSuccess("interface A<O1>{O1 get(O1 x);}; A a = (X)->X; Object x = new Object(); assert a.get(x) == x;");
		assertSuccess("interface A<O2>{O2 get(O2 x);}; A a = (Object X)->X; Object x = new Object(); assert a.get(x) == x;");
		assertFailCompile("interface A<O3>{O3 get(O3 x);}; A a = (String X)->X;", //
				"incompatible parameter types in lambda expression: expected Object but found String");

		assertSuccess("interface A<O>{O get(O x);}; A<String> a = (X)->X + \"b\"; assert a.get(\"a\").equals(\"ab\");");
		assertSuccess("interface A<O>{O get(O x);}; A<String> a = (String X)->X + \"b\"; assert a.get(\"a\").equals(\"ab\");");
		assertFailCompile("interface A<O>{O get(O x);}; A<String> a = (Object X)->X;", //
				"incompatible parameter types in lambda expression: expected String but found Object");

		assertSuccess("interface A<O extends Number>{O get(O x);}; A a = (X)->X;");
		assertSuccess("interface A<O extends Number>{O get(O x);}; A a = (Number X)->X;");
		assertFailCompile("interface A<O extends Number>{O get(O x);}; A a = (Integer X)->X;", //
				"incompatible parameter types in lambda expression: expected Number but found Integer");

		assertSuccess("interface A<O extends Number>{O get(O x);}; A<Integer> a = (X)->X + 1; assert a.get(1) == 2;");
		assertSuccess("interface A<O extends Number>{O get(O x);}; A<Integer> a = (Integer X)->X + 1; assert a.get(1) == 2;");
		assertFailCompile("interface A<O extends Number>{O get(O x);}; A<Integer> a = (Double X)->X;", //
				"incompatible parameter types in lambda expression: expected Integer but found Double");
		assertFailCompile("interface A<O extends Object>{O get(O x);}; A<Number> a = (Integer X)->X;", //
				"incompatible parameter types in lambda expression: expected Number but found Integer");
	}
}
