import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.compiler.ParserUtil;
import ru.nest.hiscript.ool.model.HiCompiler;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccess(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws IOException {
		assertSuccess("public @interface Field{int value() default 1 + 2;}");
		assertSuccess("abstract @interface Field{String value() default \"x=\" + 1;}");
		assertSuccess("static @interface Field{int value();}");
		assertSuccess("@interface Field{String value() default \"\"; int count() default x; int x = 1; boolean valid();}");

		//		assertSuccess("public @interface Field{String value() default \"\";} @Field int x;");
		//		assertSuccess("public @interface Field{String value() default \"\";} @Field(\"x\") int x;");
		//		assertSuccess("public @interface Field{String value() default \"\";} @Field(value = \"x\") int x;");
		//		assertSuccess("public @interface Field{String value() default \"\"; int count;} @Field(value = \"x\", count = 1) int x;");
		//		assertSuccess("public @interface Field{String value() default \"\"; int count default 0;} @Field(value = \"x\") int x;");
		//		assertSuccess("public @interface Field{int value() default v; int v = 1;} @Field int x;");
	}
}