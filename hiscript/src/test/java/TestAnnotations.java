import org.junit.jupiter.api.Test;

public class TestAnnotations extends HiTest {
	@Test
	public void test() {
		assertSuccess("public @interface Field{int value() default 1 + 2;}");
		assertSuccess("abstract @interface Field{String value() default \"x=\" + 1;}");
		assertSuccess("static @interface Field{int value();}");
		assertSuccess("@interface Field{String value() default \"\"; int count() default x; int x = 1; boolean valid();}");
		assertFailCompile("@interface {String value();}");

		assertSuccess("@interface A{int value() default y + 1; int x = 1; int y = x + 1;} @A(value=2) class C{} C c = new C();");
		assertFailCompile("@interface A{int value() default y / 0; int x = 1; int y = x + 1;}"); // division by zero while compiling
		assertSuccess("enum E{e1,e2} @interface A{E value() default e2; E e1 = E.e1; E e2 = e1;}; @A(E.e2) class C{@A(value=E.e2) void get(){}};");
		assertSuccess("static class C{final static int CONST=1;} @interface A{int value() default C.CONST;}");

		assertSuccess("@interface Field{String value() default \"\";} @Field int x;");
		assertSuccess("@interface Field{String value() default \"\";} @Field(\"x\") int x;");
		assertSuccess("@interface Field{String value() default \"\";} @Field(value = \"x\") int x;");
		assertSuccess("@interface Field{String value() default \"\"; int count();} @Field(value = \"x\", count = 1) int x;");
		assertSuccess("@interface Field{String value() default \"\"; int count() default 0;} @Field(value = \"x\") int x;");
		assertSuccess("@interface Field{int value() default v; int v = 1;} @Field int x;");

		assertSuccess("@interface M{int value() default 1;} @M class A1{} @M(2) interface A2{} @M(value = 3) record A3(int x); @M(4) enum A4{x,y,z}");
		assertSuccess("@interface M{int value() default 1;} class A{@M void m1(@M int arg){} @M(2) void m2(){} @M(value=3) void m3(){}} new A();");
		assertSuccess("@interface M{} class A1{@M int x;} new A1(); interface A2{@M int x = 1;} record A3(int i){@M int x;} enum A4{x,y,z; @M int i;}");

		assertSuccess("@interface NonNull{} for(@NonNull Object o : new Object[]{}){} for(@NonNull int i=0; i < 3; i++);");

		assertSuccess("static class C1{final static int CONST=1;} static class C2{final static int CONST=C1.CONST;} @interface A{int value() default C2.CONST;}");
		assertFailCompile("static class C{static int CONST=1;} @interface A{int value() default C.CONST;}"); // constant expected
		assertFailCompile("static class C{final int CONST=1;} @interface A{int value() default C.CONST;}"); // constant expected
		assertFailCompile("@ (value = \"x\") int x;} }");

		// array arguments
	}
}