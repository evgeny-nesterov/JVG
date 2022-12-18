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
	public void testSingle() {
		// assertSuccess("interface IX {} class A4 extends String implements IX {} String[] s = new A4[10]; System.println(\"(IX[])s: \" + (IX[])s);");
		//		assertSuccessSerialize("class O1{}; class O2 extends O1{}; assert new O2() instanceof O1;");
		//		assertSuccessSerialize("class O{} class O1 extends O{} class C{int get(O... n){return n.length;}} assert new C().get(new O1(), new O()) == 2;");
		//		assertSuccessSerialize("class A{} class B extends A{} B[] x = {new B()}; A[] y = x; assert x == y; assert y.length == 1; assert y[0] instanceof B;");
		// assertSuccess("Java.importClass(\"JByte\", \"java.lang.Byte\"); assert new JByte(127).byteValue() == 127;");
	}
}