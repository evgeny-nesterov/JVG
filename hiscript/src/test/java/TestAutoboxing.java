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

			// primitive => autobox object
			assertSuccessSerialize(T + " a = (byte)127; assert a == 127;");
			assertSuccessSerialize(T + " a = 127; assert a == 127;");
			assertSuccessSerialize(T + " a = (short)127; assert a == 127;");
			assertSuccessSerialize(T + " a = (" + t + ")127; assert a != null;");
			assertSuccessSerialize(T + " a = 127; assert a instanceof " + T + ";");
			if (!t.equals("char")) {
				assertSuccessSerialize(T + " a = null; a = 127; assert a.toString().equals(\"\" + (" + t + ")127);");
				assertSuccessSerialize(T + " a = 1; assert a instanceof Number;");
			} else {
				assertSuccessSerialize(T + " a = null; a = 127; assert a.toString().equals(\"" + (char) 127 + "\");");
				assertSuccessSerialize(T + " a = 1; assert a instanceof Character;");
			}
			assertSuccessSerialize(T + " a = 127; " + T + " b = a; assert b == a; assert b == 127;");

			// create autobox object
			assertSuccessSerialize(T + " a = new " + T + "((" + t + ")127); assert a == 127;");
			assertSuccessSerialize("var a = new " + T + "((" + t + ")127); assert a == 127;");

			// operations
			assertSuccessSerialize(T + " a = 1; " + T + " b = 2; " + T + " c = 3; assert a + b == c;");
			for (String operation : operations) {
				String B1 = "new " + T + "((" + t + ")63)";
				String b1 = "(" + t + ")63";
				String B2 = "new " + T + "((" + t + ")3)";
				String b2 = "(" + t + ")3";
				assertSuccessSerialize("assert (" + B1 + " " + operation + " " + b2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
				assertSuccessSerialize("assert (" + b1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
				assertSuccessSerialize("assert (" + B1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
			}

			assertSuccessSerialize(T + " a = 1; a++; assert a == 2;");
			assertSuccessSerialize(T + " a = 1; ++a; assert a == 2;");
			assertSuccessSerialize(T + " a = 1; a--; assert a == 0;");
			assertSuccessSerialize(T + " a = 1; --a; assert a == 0;");
			for (String operation : operations) {
				String script = T + " a = 32; a " + operation + "= 1;";
				if (allowEquatePlus) {
					assertSuccessSerialize(script);
				} else {
					assertFailCompile(script, //
							"can not be applied");
				}
			}
			assertFailCompile(T + " a = 32; a ~= 1;", //
					"not a statement");
			assertSuccessSerialize(T + " a = 1; assert a > 0;");
			assertSuccessSerialize(T + " a = 1; assert a >= 0;");
			assertSuccessSerialize(T + " a = 1; assert a < 2;");
			assertSuccessSerialize(T + " a = 1; assert a <= 2;");
			assertSuccessSerialize(T + " a = 1; assert a != 2;");
			if (cacheable) {
				assertSuccessSerialize(T + " a = 1; " + t + " b = a; " + T + " c = a; " + T + " d = b; assert a == c; assert a == d;");
				assertSuccessSerialize(T + " a = 1; " + T + " b = 1; assert a == b;"); // cached value
				assertSuccessSerialize(T + " a = 127; " + T + " b = 127; assert a == b;"); // max cached value
			}
			assertSuccessSerialize(T + " a = new " + T + "((" + t + ")1); " + T + " b = 1; assert a != b;");

			// class fields
			assertSuccessSerialize("class C{" + T + " a; C(" + T + " a) {this.a = a;}} assert new C((" + t + ")127).a == 127;");
			assertSuccessSerialize("class C{static " + T + " a;} C.a = (" + t + ")127; assert C.a == 127;");
			assertSuccessSerialize("class C{static " + t + " a;} C.a = new " + T + "((" + t + ")127); assert C.a == new " + T + "((" + t + ")127);");

			// methods + return
			assertSuccessSerialize("class C{" + t + " set(" + T + " a){return a;}} assert new C().set(new " + T + "((" + t + ")123)) == new " + T + "((" + t + ")123) : \"assert 2\";");
			assertSuccessSerialize("class C{" + T + " set(" + t + " a){return a;}} assert new C().set(new " + T + "((" + t + ")123)) == 123;");
			assertSuccessSerialize("class C{void set(" + t + "... a){assert a[1] == 127;}} new C().set(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127));");
			assertSuccessSerialize("class C{C(" + t + "... a){assert a[0] == 0; assert a[1] == 127; assert a[2] == 1;}} new C(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127), (" + t + ")1);");
			assertFailCompile("class C{C(" + t + "... a){}} new C(new " + T + "((" + t + ")0), new " + T + "((" + t + ")127), null);", //
					"constructor not found: C");

			// constructors
			assertSuccessSerialize("class C{" + t + " set(" + T + " a){assert a == 123; return a;}} assert new C().set((" + t + ")123) == 123;");
			assertSuccessSerialize("class C{" + T + " set(" + t + " a){return a;}} assert new C().set((" + t + ")123) == 123;");
			assertSuccessSerialize("class C{C(Object a){assert a instanceof " + T + ";}} new C((" + t + ")123);");
			assertSuccessSerialize("class C{C(" + T + "... a){assert a[0] instanceof " + T + "; assert a[1] instanceof " + T + "; assert a[1] == 127; assert a[2] == null; assert a[3] == 1;}} new C((" + t + ")0, (" + t + ")127, null, new " + T + "((" + t + ")1));");
			assertSuccessSerialize("class C{C(Object... a){assert a[0] instanceof " + T + "; assert a[1] instanceof " + T + "; assert (" + T + ")a[1] == 127;}} new C((" + t + ")0, (" + t + ")127);");
			assertFail("class C{void set(" + t + " a){}} " + T + " b = null; new C().set(b);", //
					"null pointer");
			assertFail("class C{C(" + t + " a){}} " + T + " b = null; new C(b);", //
					"null pointer");

			// arrays
			assertSuccessSerialize(T + "[] a = new " + T + "[3]; a[0] = (" + t + ")1; a[0]++; a[1] = 127; assert a[0] == 2; assert a[1] == 127; assert a[2] == null;");
			assertSuccessSerialize(T + "[][] a = new " + T + "[3][3]; a[0][0] = (" + t + ")1; a[0][0]++; a[1][1] = 127; assert a[0][0] == 2; assert a[1][1] == 127; assert a[2][2] == null;");
			assertSuccessSerialize("" + t + "[] a = new " + t + "[2]; a[1] = new " + T + "((" + t + ")127); assert a[1] == 127;");
			assertSuccessSerialize("" + t + "[][] a = new " + t + "[2][2]; a[1][1] = new " + T + "((" + t + ")127); assert a[1][1] == 127;");
			assertFail("" + t + "[][] a = new " + t + "[1][1]; " + T + " b = null; a[0][0] = b;", //
					"null pointer");

			// statements
			assertSuccessSerialize("for(" + T + " i = 0; i < 127; i++) {assert i instanceof " + T + ";}");
			assertSuccessSerialize(T + " i = 0; while(i < 10) i++; assert i == 10;");
			assertFailCompile("" + t + " i = 0; while(i < 10) i = null;", //
					"operator '=' can not be applied to " + t + ", null");

			assertSuccessSerialize("class A{Number x;} A a = new A(); a.x = (" + t + ")1; assert (" + t + ")a.x == 1;");
			assertFail("class A{Number x;} A a = new A(); assert (" + t + ")a.x == 1;", //
					"null pointer");
		}
	}

	@Test
	public void testByte() {
		// primitive => autobox object
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
		assertSuccessSerialize("byte a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccessSerialize("int a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccessSerialize("short a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertFailCompile("char a = new Byte((byte)0);", //
				"incompatible types: Byte cannot be converted to char");
		assertFailCompile("char a = 0; a = new Byte((byte)0);", //
				"operator '=' can not be applied to char, Byte");
		assertSuccessSerialize("long a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccessSerialize("float a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccessSerialize("double a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertFail("Byte a = null; byte b = a;", //
				"null pointer");

		// operations
		assertSuccessSerialize("Byte a = 127; a++; assert a == -128;");
		assertSuccessSerialize("Byte a = -128; a--; assert a == 127;");
	}

	@Test
	public void testShort() {
		// primitive => autobox object
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
		assertSuccessSerialize("int a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccessSerialize("short a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccessSerialize("long a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccessSerialize("float a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertSuccessSerialize("double a = new Short((short)32767); assert a == 32767; a = new Short((short)-1); assert a == -1;");
		assertFail("Short a = null; short b = a;", //
				"null pointer");

		// operations
		assertSuccessSerialize("Short a = 32767; a++; assert a == -32768;");
		assertSuccessSerialize("Short a = -32768; a--; assert a == 32767;");
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
		assertSuccessSerialize("assert Boolean.TRUE == true;");
		assertSuccessSerialize("assert Boolean.FALSE == false;");
		assertSuccessSerialize("assert Boolean.TRUE.equals(new Boolean(true));");
		assertSuccessSerialize("assert Boolean.FALSE.equals(new Boolean(false));");
		assertSuccessSerialize("assert new Boolean(true).equals(Boolean.TRUE);");
		assertSuccessSerialize("assert new Boolean(false).equals(Boolean.FALSE);");

		// create autobox object
		assertSuccessSerialize("Boolean a = new Boolean(true); assert a == true;");
		assertSuccessSerialize("var a = new Boolean(false); assert a == false;");
		assertSuccessSerialize("Boolean a = Boolean.TRUE; assert a.equals(true); assert a.equals(Boolean.TRUE);");
		assertSuccessSerialize("Boolean a = Boolean.FALSE; assert a.equals(false); assert a.equals(Boolean.FALSE);");

		// primitive => autobox object
		assertSuccessSerialize("Boolean a = true; assert a == true; assert a == Boolean.TRUE;");
		assertSuccessSerialize("Boolean a = false; assert a == false; assert a == Boolean.FALSE;");
		assertSuccessSerialize("Boolean a = true; Boolean b = true; assert a == b;"); // from cache

		// boxed object => primitive
		assertSuccessSerialize("boolean a = new Boolean(true); assert a == true;");
		assertSuccessSerialize("boolean a = new Boolean(false); assert a == Boolean.FALSE;");
		assertSuccessSerialize("boolean a = Boolean.TRUE; assert a == Boolean.TRUE;");

		// operations
		assertSuccessSerialize("Boolean a = true; Boolean b = false; assert (a || b) == true;");
		assertSuccessSerialize("Boolean a = true; Boolean b = false; assert (a && b) == false;");

		// nulls
		assertSuccessSerialize("Boolean a = false; Boolean b = null; assert (a && b) == false;");
		assertSuccessSerialize("Boolean a = true; Boolean b = null; assert (a || b) == true;");
		assertFail("Boolean a = true; Boolean b = null; assert a && b;", //
				"null pointer");
		assertFail("boolean a = false; Boolean b = null; assert a || b;", //
				"null pointer");

		assertSuccessSerialize("Boolean[] a = new Boolean[3]; a[0] = (boolean)true; a[0] = !a[0]; a[1] = true; assert a[0]; assert a[1]; assert a[2] == null;");
	}

	@Test
	public void testCharacter() {
		assertSuccessSerialize("assert new Character('a') == 'a';");
		assertSuccessSerialize("Character a = new Character('\\''); assert a == '\\'';");
		assertSuccessSerialize("Character a = 'a'; a++; assert a == 'b';");
		assertSuccessSerialize("Character a = 1001; a--; assert a == 1000;");
		assertSuccessSerialize("char a = new Character('a'); assert a == new Character('a');");
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
