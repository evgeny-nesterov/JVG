import org.junit.jupiter.api.Test;

public class TestAnnotations extends HiTest {
	@Test
	public void test() {
		assertSuccess("public @interface Field{byte value() default (byte)(1 + 2);}");
		assertSuccess("public @interface Field{short value() default (short)(1 + 2);}");
		assertSuccess("public @interface Field{char value() default (char)(1 + 2);}");
		assertSuccess("public @interface Field{int value() default 1 + 2;}");
		assertSuccess("public @interface Field{long value() default 1L + 2L;}");
		assertSuccess("public @interface Field{float value() default 1f + 2f;}");
		assertSuccess("public @interface Field{double value() default 1.0 + 1.0 * 2.0;}");
		assertSuccess("public @interface Field{boolean value() default !true || !false;}");
		assertSuccess("@interface Field{String value() default \"x=\" + 1;}");

		assertSuccess("static @interface Field{int value();}");
		assertSuccess("abstract @interface F1{String value() default \"x=\" + 1;}");
		assertSuccess("@interface Field{String value() default \"\"; int count() default x; int x = 1; boolean valid();}");

		assertSuccess("@interface A{byte value();} @A(value=(byte)2) class C{} C c = new C();");
		assertSuccess("@interface A{short value();} @A(value=(short)2) class C{} C c = new C();");
		assertSuccess("@interface A{char value();} @A(value='!') class C{} C c = new C();");
		assertSuccess("@interface A{int value();} @A(value=2) class C{} C c = new C();");
		assertSuccess("@interface A{long value();} @A(value=2L) class C{} C c = new C();");
		assertSuccess("@interface A{float value();} @A(value=2f) class C{} C c = new C();");
		assertSuccess("@interface A{double value();} @A(value=2.0) class C{} C c = new C();");
		assertSuccess("@interface A{boolean value();} @A(value=true) class C{} C c = new C();");
		assertSuccess("@interface A{String value();} @A(value=\"a\") class C{} C c = new C();");

		assertFailCompile("@interface A{Byte value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Short value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Character value();}", //
				"");
		assertFailCompile("@interface A{Integer value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Long value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Float value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Double value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{Boolean value();}", //
				"invalid type 'value' for annotation member");
		assertFailCompile("@interface A{byte value();} @A(value=new Byte((byte)1)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{short value();} @A(value=new Short((short)1)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{char value();} @A(value=new Character('!')) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{int value();} @A(value=new Integer(1)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{long value();} @A(value=new Long(1L)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{float value();} @A(value=new Float(1f)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{double value();} @A(value=new Double(1.0)) class C{}", //
				"constant expected");
		assertFailCompile("@interface A{String value();} @A(value=null) class C{}", //
				"constant expected");

		assertSuccess("@interface A{int value() default y + 1; int x = 1; int y = x + 1;} @A(value=2) class C{} C c = new C();");
		assertFailCompile("@interface A{int value() default y / 0; int x = 1; int y = x + 1;}", //
				"divide by zero");
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
		assertFailCompile("static class C{static int CONST=1;} @interface A{int value() default C.CONST;}", //
				"constant expected");
		assertFailCompile("static class C{final int CONST=1;} @interface A{int value() default C.CONST;}", //
				"constant expected");

		assertSuccess("@interface F1{} @F1 @interface F2{};");

		// array arguments
//		assertSuccessSerialize("@interface A{int[] value();} @A(value={}) class C{} C c = new C();");
//		assertSuccessSerialize("@interface A{int[] value();} @A(value={0}) class C{} C c = new C();");
//		assertFailCompile("@interface A{int[] value();} @A(value=null) class C{}", //
//				"constant value expected");
//		assertFailCompile("@interface A{int[][] value();}", //
//				"?");

		// fails
		assertFailCompile("@;", //
				"unexpected token");
		assertFailCompile("@interface;", //
				"annotation class name is expected");
		assertFailCompile("@interface {String value();}", //
				"annotation class name is expected");
		assertFailCompile("@interface A;", //
				"'{' is expected");
		assertFailCompile("@interface A{", //
				"'}' is expected");
		assertFailCompile("@interface A{int value()}", //
				"';' is expected");
		assertFailCompile("@interface A{int value() default;}", //
				"value expected");
		assertFailCompile("@interface A{int value default 1;}", //
				"'}' is expected");
		assertFailCompile("@interface A{int value(int x);}", //
				"')' is expected");
		assertFailCompile("@interface A{int value() default x;}", //
				"cannot resolve symbol 'x'");
		assertFailCompile("@interface A{int value() default true;}", //
				"boolean cannot be converted to int");
		assertFailCompile("@interface A{byte value() default 1L;}", //
				"long cannot be converted to byte");
		assertFailCompile("@interface A{String value() default 'a';}", //
				"char cannot be converted to String");
		assertFailCompile("@interface A{int x;}", //
				"variable 'x' might not have been initialized");

		assertFailCompile("@(value = \"x\") int x;", //
				"annotation name is expected");
		assertFailCompile("@A(value}", //
				"')' is expected");
		assertFailCompile("@A(value = }", //
				"argument value expected");
		assertFailCompile("@A(value = null}", //
				"';' is expected");
		assertFailCompile("@A(value = 0;}", //
				"')' is expected");
		assertFailCompile("@A(value = \"x\") {};", //
				"cannot resolve symbol 'value'");
		assertFailCompile("@interface A{} int x = 0; @A x;", //
				"not a statement");
		assertFailCompile("@interface A{int value()}", //
				"';' is expected");
		assertFailCompile("@interface A{int value();} @A(a=\"a\") int x = 0;", //
				"annotation argument with name 'a' is not found");
		assertFailCompile("@interface A{int value();} @A(value=) int x = 0;", //
				"argument value expected");
		assertFailCompile("@interface A{int value();} @A(value=\"a\") int x = 0;", //
				"String cannot be converted to int");
		assertFailCompile("@interface A{int value();} @A(\"a\") int x = 0;", //
				"String cannot be converted to int");
		assertFailCompile("class A{} @A int x = 0;", //
				"annotation class expected");
		assertFailCompile("interface A{} @A int x = 0;", //
				"annotation class expected");
		assertFailCompile("public @interface A{int value();} @A(value=\"a\", value=\"b\") int x;", //
				"duplicate annotation argument");
		assertFailCompile("@interface F1{} @interface F2 extends F1 {}", //
				"'{' is expected"); // TODO @interface may not have extends list
		assertFailCompile("@interface F1{} @interface F2 implements F1 {}", //
				"'{' is expected"); // TODO no implements clause allowed for interface
		assertFailCompile("@A class B{}", //
				"cannot resolve class 'A'");
	}
}