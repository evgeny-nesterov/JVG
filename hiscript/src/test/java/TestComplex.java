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
		assertSuccessSerialize(ParserUtil.readString(HiCompiler.class.getResourceAsStream("/test/ool/oolTestFully.hi")));
	}

	@Test
	public void testSingle() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
	}

	@Test
	public void testTODO() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		//		assertFailCompile("switch(\"\"){case String s: break;}"); // default required
		//		assertFailCompile("switch(\"\"){case Object o: break; case String s: break;}"); // Object before String
		//		assertFailCompile("switch(\"\"){case Integer i: break; case String s: break;}"); // not all cases
	}

	@Test
	public void testNew() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		// lambda
		assertSuccessSerialize("interface A{int m(int x);} A a = (_) -> 1; assert a.m(0) == 1;");
		assertSuccessSerialize("interface A{int m(int x);} A a = _ -> 1; assert a.m(0) == 1;");
		assertSuccessSerialize("interface A{int m(int x, int y);} A a = (_, _) -> 1; assert a.m(0, 0) == 1;");
		assertFailCompile("interface A{int m(int x, int y);} A a = (_, _) -> _;", //
				"unnamed variable cannot be used in expressions");

		// class
		assertFailCompile("class _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{void _(){}};", //
				"keyword '_' cannot be used as an identifier");

		// interface
		assertFailCompile("interface _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("interface A{void _();};", //
				"keyword '_' cannot be used as an identifier");

		// annotation
		assertFailCompile("@interface _{};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("@interface A{int _();};", //
				"keyword '_' cannot be used as an identifier");

		// enum
		assertFailCompile("enum _{a, b, c};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("enum E{_};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("enum E{_, _};", //
				"keyword '_' cannot be used as an identifier");

		// record
		assertFailCompile("record _(int x);", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("record R(int _);", //
				"keyword '_' cannot be used as an identifier");

		// field
		assertFailCompile("int _;", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("int _ = 1;", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{int _;};", //
				"keyword '_' cannot be used as an identifier");
		assertFailCompile("class A{int _ = 1;};", //
				"keyword '_' cannot be used as an identifier");

		// instanceof
		assertSuccessSerialize("class A{int x = 1;} Object o = new A(); if(o instanceof A _) {}");
		assertSuccessSerialize("record R(int x, int y); Object o = new R(1, 2); if(o instanceof R _) {}");
		assertSuccessSerialize("record R(int x, int y); Object o = new R(1, 2); if(o instanceof R(int _, int YYY) r && YYY == 2) {assert YYY == 2; YYY = 3; assert r.getY() == 3;}");
		assertSuccessSerialize("record R(int x); Object o = new R(1); if(o instanceof R(int _) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccessSerialize("record R(int x); Object o = new R(1); if(o instanceof R(var _) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccessSerialize("record R(int x); Object o = new R(1); if(o instanceof R(_) r && r.getX() == 1) {assert r.getX() == 1;}");
		assertSuccessSerialize("record R(int x, int y, int z); Object o = new R(1, 2, 3); if(o instanceof R(_, y, _)) {assert y == 2;}");
	}
}