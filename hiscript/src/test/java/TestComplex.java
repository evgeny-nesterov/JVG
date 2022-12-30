import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.HiCompiler;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws Exception {
		assertSuccess("enum E{e1,e2} @interface A{E value() default e2; E e1 = E.e1; E e2 = e1;}; @A(E.e2) class C{@A(value=E.e2) void get(){}};");
	}
}
