import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestAnnotations extends HiTest {
	@Test
	public void test() throws IOException {
		assertSuccess("public @interface Field{int value() default 1 + 2;}");
		assertSuccess("abstract @interface Field{String value() default \"x=\" + 1;}");
		assertSuccess("static @interface Field{int value();}");
		assertSuccess("@interface Field{String value() default \"\"; int count() default x; int x = 1; boolean valid();}");

		assertSuccess("@interface Field{String value() default \"\";} @Field int x;");
		assertSuccess("@interface Field{String value() default \"\";} @Field(\"x\") int x;");
		assertSuccess("@interface Field{String value() default \"\";} @Field(value = \"x\") int x;");
		assertSuccess("@interface Field{String value() default \"\"; int count();} @Field(value = \"x\", count = 1) int x;");
		assertSuccess("@interface Field{String value() default \"\"; int count() default 0;} @Field(value = \"x\") int x;");
		assertSuccess("@interface Field{int value() default v; int v = 1;} @Field int x;");

		assertSuccess("@interface M{int value() default 1;} @M class A1{} @M(2) interface A2{} @M(value = 3) record A3(int x); @M(4) enum A4{x,y,z}");
		assertSuccess("@interface M{int value() default 1;} class A{@M void m1(@M int arg){} @M(2) void m2(){} @M(value=3) void m3(){}}");
		assertSuccess("@interface M{} class A1{@M int x;} interface A2{@M int x = 1;} record A3(int i){@M int x;} enum A4{x,y,z; @M int i;}");

		assertSuccess("@interface NonNull{} for(@NonNull Object o : new Object[]{}){}");
	}
}