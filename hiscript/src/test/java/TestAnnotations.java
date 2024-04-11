import org.junit.jupiter.api.Test;

public class TestAnnotations extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("public @interface Field{int value() default 1 + 2;}");
		assertSuccessSerialize("abstract @interface Field{String value() default \"x=\" + 1;}");
		assertSuccessSerialize("static @interface Field{int value();}");
		assertSuccessSerialize("@interface Field{String value() default \"\"; int count() default x; int x = 1; boolean valid();}");
		assertFailCompile("@interface {String value();}");

		assertSuccessSerialize("@interface A{int value() default y + 1; int x = 1; int y = x + 1;} @A(value=2) class C{} C c = new C();");
		assertFailCompile("@interface A{int value() default y / 0; int x = 1; int y = x + 1;}"); // division by zero while compiling
		assertSuccessSerialize("enum E{e1,e2} @interface A{E value() default e2; E e1 = E.e1; E e2 = e1;}; @A(E.e2) class C{@A(value=E.e2) void get(){}};");
		assertSuccessSerialize("static class C{final static int CONST=1;} @interface A{int value() default C.CONST;}");

		assertSuccessSerialize("@interface Field{String value() default \"\";} @Field int x;");
		assertSuccessSerialize("@interface Field{String value() default \"\";} @Field(\"x\") int x;");
		assertSuccessSerialize("@interface Field{String value() default \"\";} @Field(value = \"x\") int x;");
		assertSuccessSerialize("@interface Field{String value() default \"\"; int count();} @Field(value = \"x\", count = 1) int x;");
		assertSuccessSerialize("@interface Field{String value() default \"\"; int count() default 0;} @Field(value = \"x\") int x;");
		assertSuccessSerialize("@interface Field{int value() default v; int v = 1;} @Field int x;");

		assertSuccessSerialize("@interface M{int value() default 1;} @M class A1{} @M(2) interface A2{} @M(value = 3) record A3(int x); @M(4) enum A4{x,y,z}");
		assertSuccessSerialize("@interface M{int value() default 1;} class A{@M void m1(@M int arg){} @M(2) void m2(){} @M(value=3) void m3(){}} new A();");
		assertSuccessSerialize("@interface M{} class A1{@M int x;} new A1(); interface A2{@M int x = 1;} record A3(int i){@M int x;} enum A4{x,y,z; @M int i;}");

		assertSuccessSerialize("@interface NonNull{} for(@NonNull Object o : new Object[]{}){} for(@NonNull int i=0; i < 3; i++);");

		assertSuccessSerialize("static class C1{final static int CONST=1;} static class C2{final static int CONST=C1.CONST;} @interface A{int value() default C2.CONST;}");
		assertFailCompile("static class C{static int CONST=1;} @interface A{int value() default C.CONST;}"); // constant expected
		assertFailCompile("static class C{final int CONST=1;} @interface A{int value() default C.CONST;}"); // constant expected

		// array arguments

		// fails
		assertFailCompile("@;");
		assertFailCompile("@interface;");
		assertFailCompile("@interface A;");
		assertFailCompile("@interface A{");
		assertFailCompile("@interface A{int value()}");
		assertFailCompile("@interface A{int value() default;}");
		assertFailCompile("@interface A{int value default 1;}");
		assertFailCompile("@interface A{int value(int x);}");
		assertFailCompile("@interface A{int value() default x;}");
		assertFailCompile("@interface A{int value() default true;}");
		assertFailCompile("@interface A{byte value() default 1L;}");
		assertFailCompile("@interface A{String value() default 'a';}");
		assertFailCompile("@interface A{int x;}");

		assertFailCompile("@(value = \"x\") int x;");
		assertFailCompile("@A(value}");
		assertFailCompile("@A(value = }");
		assertFailCompile("@A(value = null}");
		assertFailCompile("@A(value = 0;}");
		assertFailCompile("@A(value = \"x\") {};");
		assertFailCompile("@interface A{} int x = 0; @A x;");
		assertFailCompile("@interface A{int value()}");
		assertFailCompile("@interface A{int value();} @A(a=\"a\") int x = 0;");
		assertFailCompile("@interface A{int value();} @A(value=) int x = 0;");
		assertFailCompile("@interface A{int value();} @A(value=\"a\") int x = 0;");
		assertFailCompile("@interface A{int value();} @A(\"a\") int x = 0;");
	}
}