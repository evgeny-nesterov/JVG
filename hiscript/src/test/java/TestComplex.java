import org.junit.jupiter.api.Test;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertSuccessSerialize("class E1 extends Exception{E1(){} E1(String msg){super(msg);}} class E2 extends Exception{E2(){} E2(String msg){super(msg);}} try{throw new E2(\"error\");} catch(E1 | E2 e){assert e.getMessage().equals(\"error\");}");
		assertSuccessSerialize("class A{int x;}; A a=new A(); var t=new Thread(){public void run(){sleep(100); a.x++; assert a.x==1;}}; t.start(); assert a.x==0; t.join(); assert a.x==1;");
		assertSuccessSerialize("class A{static int x; static{x += B.x+1;}} class B{static int x; static{x += A.x+1;}} new B(); assert A.x == 1; assert B.x == 2;");
		assertSuccessSerialize("class C1{} class C2 extends C1{} class C3 extends C2{} class A<O>{void m(O o){}} " + //
				"A<C1> x_ = new A<>(); x_.m(new C1()); x_.m(new C2()); x_.m(new C3()); " + //
				"A<? super C1> x = new A<>(); x = new A<C1>(); x = x_; x.m(new C1()); x.m(new C2()); x.m(new C3());");
		assertSuccessSerialize("class A{<O> O m(O x) {return x;}} assert new A().m(123) == 123;");
		assertSuccessSerialize("interface A<O>{O get();} assert new A<Byte>(){Byte get(){return 123;}}.get() == 123;");
		assertSuccessSerialize("class C{<O> O get(){return (O)new Integer(1);}} Integer v = new C().get(); assert v == 1;");
		assertSuccessSerialize("class C{<O> O get(){return (O)\"abc\";}} String abc = new C().get(); assert abc.equals(\"abc\");");
		assertSuccessSerialize("class A<O>{O m(O x) {return x;}} class B extends A<Integer>{} assert new B().m(123) == 123;");
		assertSuccessSerialize("interface I{interface I1{}} class X implements I {I1 m() {I1 i = new I1(){}; return i;}} assert new X().m() instanceof I.I1;");
		assertSuccessSerialize("class O{} class C{Object[] o; C(Object... o){this.o = o;}} assert new C().o.length == 0; assert new C(new O(), \"x\", new Object()).o.length == 3;;");
		assertSuccessSerialize("class A{static String a = B.b + 'A';} class B{static String b = A.a + 'B';} assert A.a.equals(\"nullBA\"); assert B.b.equals(\"nullB\");");
		assertSuccessSerialize("assert new Object(){public String toString(){return \"a\";}}.toString().equals(\"a\");");
		assertSuccessSerialize("interface A{int length();} A a = \"abc\"::length; assert a.length() == 3;"); // same method name
		assertSuccessSerialize("class C{int c = 0;} C c = new C(); interface I{void m(I i, int x);} I o = (i,x)->{if(x>0) i.m(i, x-1); c.c++;}; o.m(o, 5); assert c.c == 6;");
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} class D{void d(){}} new C().m(new D()::d);");
		assertSuccessSerialize("interface I{void get();} class C{void m(I i){i.get();}} new C().m(()->{});");
		assertSuccessSerialize("class A<O>{O o; A(O o){this.o=o;} O get(){return o;}} class B{<O> O get(A<O> a){return a.get();}} A<Integer> a = new A<>(1); Integer v = new B().get(a); assert v == 1;");
		assertSuccessSerialize("class A<O>{O value; void set(O value){this.value=value;} O get(){return value;}}; A<Integer> a = new A<>(); a.set(1); assert a.get() == 1;");
		assertSuccessSerialize("class A<O>{O value; A(O value){this.value = value;}} assert new A<Boolean>(true).value; assert new A<Integer>(123).value == 123;");
	}

	@Test
	public void testTodo() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
//		TODO assertFailCompile("switch(\"\"){case Integer i, String s: break;}"); // several types in one case
//		TODO assertFailCompile("switch(\"\"){case String s: break;}"); // default required
//		TODO assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
//		TODO assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
	}
}