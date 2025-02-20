import org.junit.jupiter.api.Test;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class TestCompileValues extends HiTest {
	@Test
	public void testCompileValue() throws HiScriptParseException, TokenizerException, HiScriptValidationException {
		assertSuccess("class A{final static Boolean x = true;} assert A.x == Boolean.TRUE;");
		assertSuccess("class A{final static boolean x = !false;} assert A.x;");

		for (String t : new String[] {"byte", "short", "char", "int", "long", "float", "double"}) {
			assertSuccess("class A{final " + t + " x = (byte)1;} assert new A().x == 1;");
			assertSuccess("class A{final " + t + " x = (short)1;} assert new A().x == 1;");
			assertSuccess("class A{final " + t + " x = (char)1;} assert new A().x == 1;");
			assertSuccess("class A{final " + t + " x = (int)1;} assert new A().x == 1;");

			for (String t1 : new String[] {"byte", "short", "char", "int"}) {
				assertSuccess("class A{final " + t + " x = (" + t1 + ")(1+2);} assert new A().x == 3;");
				for (String t2 : new String[] {"byte", "short", "char", "int"}) {
					assertSuccess("class A{final " + t + " x = (" + t1 + ")1+(" + t2 + ")2;} assert new A().x == 3;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")1+(" + t1 + ")2;} assert new A().x == 3;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")2-(" + t2 + ")1;} assert new A().x == 1;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")2-(" + t1 + ")1;} assert new A().x == 1;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")2*(" + t2 + ")3;} assert new A().x == 6;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")2*(" + t1 + ")3;} assert new A().x == 6;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")6/(" + t2 + ")3;} assert new A().x == 2;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")6/(" + t1 + ")3;} assert new A().x == 2;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")5%(" + t2 + ")3;} assert new A().x == 2;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")5%(" + t1 + ")3;} assert new A().x == 2;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")7&(" + t2 + ")2;} assert new A().x == 2;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")7&(" + t1 + ")2;} assert new A().x == 2;");

					assertSuccess("class A{final " + t + " x = (" + t1 + ")5|(" + t2 + ")2;} assert new A().x == 7;");
					assertSuccess("class A{final " + t + " x = (" + t2 + ")5|(" + t1 + ")2;} assert new A().x == 7;");
				}
			}

			assertSuccess("class A{final double x = (" + t + ")(1+2);} assert new A().x == 3;");

			assertFailCompile("class A{final " + t + " x = true;}", //
					"incompatible types; found boolean, required " + t);
		}

		assertFailCompile("class A{final byte x = 1L;}", //
				"incompatible types; found long, required byte");
		assertFailCompile("class A{final byte x = 1f;}", //
				"incompatible types; found float, required byte");
		assertFailCompile("class A{final byte x = 1.0;}", //
				"incompatible types; found double, required byte");
		assertFailCompile("class A{final byte x = 128;}", //
				"incompatible types; found int, required byte");

		assertFailCompile("class A{final short x = 1L;}", //
				"incompatible types; found long, required short");
		assertFailCompile("class A{final short x = 1f;}", //
				"incompatible types; found float, required short");
		assertFailCompile("class A{final short x = 1.0;}", //
				"incompatible types; found double, required short");
		assertFailCompile("class A{final short x = " + (Short.MAX_VALUE + 1) + ";}", //
				"incompatible types; found int, required short");

		assertFailCompile("class A{final char x = 1L;}", //
				"incompatible types; found long, required char");
		assertFailCompile("class A{final char x = 1f;}", //
				"incompatible types; found float, required char");
		assertFailCompile("class A{final char x = 1.0;}", //
				"incompatible types; found double, required char");
		assertFailCompile("class A{final char x = " + (Character.MAX_VALUE + 1) + ";}", //
				"incompatible types; found int, required char");

		assertFailCompile("class A{final int x = 1L;}", //
				"incompatible types; found long, required int");
		assertFailCompile("class A{final int x = 1f;}", //
				"incompatible types; found float, required int");
		assertFailCompile("class A{final int x = 1.0;}", //
				"incompatible types; found double, required int");
		assertFailCompile("class A{final int x = " + (Integer.MAX_VALUE + 1L) + ";}", //
				"integer number too large");

		assertSuccess("class A{final long x = 1L;}");
		assertFailCompile("class A{final long x = 1f;}", //
				"incompatible types; found float, required long");
		assertFailCompile("class A{final long x = 1.0;}", //
				"incompatible types; found double, required long");

		assertSuccess("class A{final float x = 1L;}");
		assertSuccess("class A{final float x = 1f;}");
		assertFailCompile("class A{final float x = 1.0;}", //
				"incompatible types; found double, required float");

		assertSuccess("class A{final double x = 1L;}");
		assertSuccess("class A{final double x = 1f;}");
		assertSuccess("class A{final double x = 1.0;}");
	}
}
