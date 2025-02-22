import org.junit.jupiter.api.Test;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.compile.ParserUtil;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class TestComplex extends HiTest {
	@Test
	public void testFull() throws IOException {
		assertSuccess(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertSuccess("interface A{int getX();} class B implements A {int x; B(int x){this.x = x;} int getX(){return x;} int getY(){return x + 1;}}\n" + //
				"record R<O extends A>(O a); Object o = new R<B>(new B(1));\n" + //
				"if(o instanceof R<B> r) {assert r.getA().getY() == 2; return;} assert false;");
		assertFailCompile("interface A{int getX();} class B implements A {int x; B(int x){this.x = x;} int getX(){return x;} int getY(){return x + 1;}}\n" + //
						"record R<O extends A>(O a); Object o = new R<B>(new B(1));\n" + //
						"if(o instanceof R<A> r) {int y = r.getA().getY()}",
				"cannot resolve method 'getY' in 'A'");
		assertFailCompile("interface A<O>{O get();} class B implements A<Integer>{Integer get(){return 1;}} class C implements A<String>{String get(){return \"a\";}} " + //
						"record R<O extends A>(O a); Object o = new R<B>(new B());" + //
						"if(o instanceof R<C> r) {Integer value = r.get();}", //
				"incompatible return type");
		assertFailCompile("interface A<O>{O get();} class B implements A<Integer>{Integer get(){return 1;}} class C implements A<String>{String get(){return \"a\";}} " + //
						"record R<O extends A>(O a); Object o = new R<B>(new B());" + //
						"if(o instanceof R<C> r) {String value = r.get();}", //
				"incompatible return type");
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
		//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
		//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
	}
}