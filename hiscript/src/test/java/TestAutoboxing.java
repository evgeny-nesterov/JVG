import org.junit.jupiter.api.Test;

public class TestAutoboxing extends HiTest {
	String[] operations = {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"};

	String[] intOperations = {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"};

	String[] floatOperations = {"+", "-", "*", "/", "%"};

	String[] primitiveNumberTypes = {"byte", "short", "char", "int", "long", "float", "double"};

	String[] primitiveNumberBoxedTypes = {"Byte", "Short", "Character", "Integer", "Long", "Float", "Double"};

	boolean[] intTypes = {true, true, true, true, true, false, false};

	boolean[] allowEquatePlus = {false, false, false, true, true, true, true};

	boolean[] cacheable = {true, true, false, true, true, false, false};

	@Test
	public void testCommonNumbers() {
		for (int i = 0; i < primitiveNumberTypes.length; i++) {
			String t = primitiveNumberTypes[i];
			String T = primitiveNumberBoxedTypes[i];
			boolean intNumber = intTypes[i];
			String[] operations = intNumber ? intOperations : floatOperations;
			boolean allowEquatePlus = this.allowEquatePlus[i];
			boolean cacheable = this.cacheable[i];

			// primitive => autoboxing object
			assertSuccess(T + " a = (byte)127; assert a == 127;");
			assertSuccess(T + " a = 127; assert a == 127;");
			assertSuccess(T + " a = (short)127; assert a == 127;");
			assertSuccess(T + " a = (" + t + ")127; assert a != null;");
			assertSuccess(T + " a = 127; assert a instanceof " + T + ";");
			if (!t.equals("char")) {
				assertSuccess(T + " a = null; a = 127; assert a.toString().equals(\"\" + (" + t + ")127);");
				assertSuccess(T + " a = 1; assert a instanceof Number;");
			} else {
				assertSuccess(T + " a = null; a = 127; assert a.toString().equals(\"" + (char) 127 + "\");");
				assertSuccess(T + " a = 1; assert a instanceof Character;");
			}
			assertSuccess(T + " a = 127; " + T + " b = a; assert b == a; assert b == 127;");

			// create autoboxing object
			assertSuccess(T + " a = new " + T + "((" + t + ")127); assert a == 127;");
			assertSuccess("var a = new " + T + "((" + t + ")127); assert a == 127;");

			// operations
			assertSuccess(T + " a = 1; " + T + " b = 2; " + T + " c = 3; assert a + b == c;");
			for (String operation : operations) {
				String B1 = "new " + T + "((" + t + ")63)";
				String b1 = "(" + t + ")63";
				String B2 = "new " + T + "((" + t + ")3)";
				String b2 = "(" + t + ")3";
				assertSuccess("assert (" + B1 + " " + operation + " " + b2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
				assertSuccess("assert (" + b1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
				assertSuccess("assert (" + B1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
			}

			assertSuccess(T + " a = 1; a++; assert a == 2;");
			assertSuccess(T + " a = 1; ++a; assert a == 2;");
			assertSuccess(T + " a = 1; a--; assert a == 0;");
			assertSuccess(T + " a = 1; --a; assert a == 0;");
			for (String operation : operations) {
				String script = T + " a = 32; a " + operation + "= 1;";
				if (allowEquatePlus) {
					assertSuccess(script);
				} else {
					assertFailCompile(script, //
							"can not be applied");
				}
			}
			assertFailCompile(T + " a = 32; a ~= 1;", //
					"not a statement");
			assertSuccess(T + " a = 1; assert a > 0;");
			assertSuccess(T + " a = 1; assert a >= 0;");
			assertSuccess(T + " a = 1; assert a < 2;");
			assertSuccess(T + " a = 1; assert a <= 2;");
			assertSuccess(T + " a = 1; assert a != 2;");
			if (cacheable) {
				assertSuccess(T + " a = 1; " + t + " b = a; " + T + " c = a; " + T + " d = b; assert a == c; assert a == d;");
				assertSuccess(T + " a = 1; " + T + " b = 1; assert a == b;"); // cached value
				assertSuccess(T + " a = 127; " + T + " b = 127; assert a == b;"); // max cached value
			}
			assertSuccess(T + " a = new " + T + "((" + t + ")1); " + T + " b = 1; assert a != b;");

			// class fields
			assertSuccess("class C{" + T + " a; C(" + T + " a) {this.a = a;}} assert new C((" + t + ")127).a == 127;");
			assertSuccess("class C{static " + T + " a;} C.a = (" + t + ")127; assert C.a == 127;");
			assertSuccess("class C{static " + t + " a;} C.a = new " + T + "((" + t + ")127); assert C.a == new " + T + "((" + t + ")127);");

			// methods + return
			assertSuccess("class C{" + t + " set(" + T + " a){return a;}} assert new C().set(new " + T + "((" + t + ")123)) == new " + T + "((" + t + ")123) : \"assert 2\";");
			assertSuccess("class C{" + T + " set(" + t + " a){return a;}} assert new C().set(new " + T + "((" + t + ")123)) == 123;");
			assertSuccess("class C{void set(" + t + "... a){assert a[1] == 127;}} new C().set(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127));");
			assertSuccess("class C{C(" + t + "... a){assert a[0] == 0; assert a[1] == 127; assert a[2] == 1;}} new C(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127), (" + t + ")1);");
			assertFailCompile("class C{C(" + t + "... a){}} new C(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127), null);", //
					"constructor not found: C");

			// constructors
			assertSuccess("class C{" + t + " set(" + T + " a){assert a == 123; return a;}} assert new C().set((" + t + ")123) == 123;");
			assertSuccess("class C{" + T + " set(" + t + " a){return a;}} assert new C().set((" + t + ")123) == 123;");
			assertSuccess("class C{C(Object a){assert a instanceof " + T + ";}} new C((" + t + ")123);");
			assertSuccess("class C{C(" + T + "... a){assert a[0] instanceof " + T + "; assert a[1] instanceof " + T + "; assert a[1] == 127; assert a[2] == null; assert a[3] == 1;}} new C((" + t + ")0, (" + t + ")127, null, new " + T + "((" + t + ")1));");
			assertSuccess("class C{C(Object... a){assert a[0] instanceof " + T + "; assert a[1] instanceof " + T + "; assert (" + T + ")a[1] == 127;}} new C((" + t + ")0, (" + t + ")127);");
			assertFail("class C{void set(" + t + " a){}} " + T + " b = null; new C().set(b);", //
					"null pointer");
			assertFail("class C{C(" + t + " a){}} " + T + " b = null; new C(b);", //
					"null pointer");

			// arrays
			assertSuccess(T + "[] a = new " + T + "[3]; a[0] = (" + t + ")1; a[0]++; a[1] = 127; assert a[0] == 2; assert a[1] == 127; assert a[2] == null;");
			assertSuccess(T + "[][] a = new " + T + "[3][3]; a[0][0] = (" + t + ")1; a[0][0]++; a[1][1] = 127; assert a[0][0] == 2; assert a[1][1] == 127; assert a[2][2] == null;");
			assertSuccess("" + t + "[] a = new " + t + "[2]; a[1] = new " + T + "((" + t + ")127); assert a[1] == 127;");
			assertSuccess("" + t + "[][] a = new " + t + "[2][2]; a[1][1] = new " + T + "((" + t + ")127); assert a[1][1] == 127;");
			assertFail("" + t + "[][] a = new " + t + "[1][1]; " + T + " b = null; a[0][0] = b;", //
					"null pointer");

			// statements
			assertSuccess("for(" + T + " i = 0; i < 127; i++) {assert i instanceof " + T + ";}");
			assertSuccess(T + " i = 0; while(i < 10) i++; assert i == 10;");
			assertFailCompile("" + t + " i = 0; while(i < 10) i = null;", //
					"operator '=' can not be applied to " + t + ", null");

			assertSuccess("class A{Number x;} A a = new A(); a.x = (" + t + ")1; assert (" + t + ")a.x == 1;");
			assertFail("class A{Number x;} A a = new A(); assert (" + t + ")a.x == 1;", //
					"null pointer");
		}
	}

	@Test
	public void testByte() {
		// primitive => autoboxing object
		assertFailCompile("Byte a = 128;", //
				"incompatible types: int cannot be converted to Byte");
		assertFailCompile("Byte a = 1L;", //
				"incompatible types: long cannot be converted to Byte");
		assertFailCompile("Byte a = (short)128;", //
				"incompatible types: short cannot be converted to Byte");
		assertFailCompile("Byte a = 1.1;", //
				"incompatible types: double cannot be converted to Byte");
		assertFailCompile("Byte a = 1f;", //
				"incompatible types: float cannot be converted to Byte");
		assertFailCompile("Byte a = true;", //
				"incompatible types: boolean cannot be converted to Byte");

		// boxed object => primitive
		assertSuccess("byte a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("int a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("short a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertFailCompile("char a = new Byte((byte)0);", //
				"incompatible types: Byte cannot be converted to char");
		assertFailCompile("char a = 0; a = new Byte((byte)0);", //
				"operator '=' can not be applied to char, Byte");
		assertSuccess("long a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("float a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("double a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertFail("Byte a = null; byte b = a;", //
				"null pointer");

		// operations
		assertSuccess("Byte a = 127; a++; assert a == -128;");
		assertSuccess("Byte a = -128; a--; assert a == 127;");
	}

	@Test
	public void testShort() {
		// primitive => autoboxing object
		assertFailCompile("Short a = " + (Short.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to Short");
		assertFailCompile("Short a = 1L;", //
				"incompatible types: long cannot be converted to Short");
		assertFailCompile("Short a = (int)" + (Short.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to Short");
		assertFailCompile("Short a = 1.1;", //
				"incompatible types: double cannot be converted to Short");
		assertFailCompile("Short a = 1f;", //
				"incompatible types: float cannot be converted to Short");
		assertFailCompile("Short a = true;", //
				"incompatible types: boolean cannot be converted to Short");

		// boxed object => primitive
		assertFailCompile("byte a = new Short((short)127);", //
				"incompatible types: Short cannot be converted to byte");
		assertSuccess("int a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccess("short a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccess("long a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccess("float a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccess("double a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertFail("Short a = null; short b = a;", //
				"null pointer");

		// operations
		assertSuccess("Short a = 32767; a++; assert a == -32768;");
		assertSuccess("Short a = -32768; a--; assert a == 32767;");
	}

	@Test
	public void testInteger() {
	}

	@Test
	public void testLong() {
	}

	@Test
	public void testFloat() {
	}

	@Test
	public void testDouble() {
	}

	@Test
	public void testBoolean() {
		// constants TRUE and FALSE
		assertSuccess("assert Boolean.TRUE == true;");
		assertSuccess("assert Boolean.FALSE == false;");
		assertSuccess("assert Boolean.TRUE.equals(new Boolean(true));");
		assertSuccess("assert Boolean.FALSE.equals(new Boolean(false));");
		assertSuccess("assert new Boolean(true).equals(Boolean.TRUE);");
		assertSuccess("assert new Boolean(false).equals(Boolean.FALSE);");

		// create autoboxing object
		assertSuccess("Boolean a = new Boolean(true); assert a == true;");
		assertSuccess("var a = new Boolean(false); assert a == false;");
		assertSuccess("Boolean a = Boolean.TRUE; assert a.equals(true); assert a.equals(Boolean.TRUE);");
		assertSuccess("Boolean a = Boolean.FALSE; assert a.equals(false); assert a.equals(Boolean.FALSE);");

		// primitive => autoboxing object
		assertSuccess("Boolean a = true; assert a == true; assert a == Boolean.TRUE;");
		assertSuccess("Boolean a = false; assert a == false; assert a == Boolean.FALSE;");
		assertSuccess("Boolean a = true; Boolean b = true; assert a == b;"); // from cache

		// boxed object => primitive
		assertSuccess("boolean a = new Boolean(true); assert a == true;");
		assertSuccess("boolean a = new Boolean(false); assert a == Boolean.FALSE;");
		assertSuccess("boolean a = Boolean.TRUE; assert a == Boolean.TRUE;");

		// operations
		assertSuccess("Boolean a = true; Boolean b = false; assert (a || b) == true;");
		assertSuccess("Boolean a = true; Boolean b = false; assert (a && b) == false;");

		// nulls
		assertSuccess("Boolean a = false; Boolean b = null; assert (a && b) == false;");
		assertSuccess("Boolean a = true; Boolean b = null; assert (a || b) == true;");
		assertFail("Boolean a = true; Boolean b = null; assert a && b;", //
				"null pointer");
		assertFail("boolean a = false; Boolean b = null; assert a || b;", //
				"null pointer");

		assertSuccess("Boolean[] a = new Boolean[3]; a[0] = (boolean)true; a[0] = !a[0]; a[1] = true; assert a[0]; assert a[1]; assert a[2] == null;");
	}

	@Test
	public void testCharacter() {
		assertSuccess("assert new Character('a') == 'a';");
		assertSuccess("Character a = new Character('\\''); assert a == '\\'';");
		assertSuccess("Character a = 'a'; a++; assert a == 'b';");
		assertSuccess("Character a = 1001; a--; assert a == 1000;");
		assertSuccess("char a = new Character('a'); assert a == new Character('a');");
		assertFailCompile("Character a = -1;", //
				"int cannot be converted to Character");
		assertFailCompile("Character a = -1; a = -a;", //
				"int cannot be converted to Character");
		assertFailCompile("Character a = -1; a *= -1;", //
				"int cannot be converted to Character");
		assertFailCompile("Character a = -1; a /= -1;", //
				"int cannot be converted to Character");
		assertFailCompile("Character a = -1; a /= -1;", //
				"int cannot be converted to Character");
		assertFailCompile("Character a = 1; a *= 'x';", //
				"operator '*=' can not be applied to Character");
	}
}
