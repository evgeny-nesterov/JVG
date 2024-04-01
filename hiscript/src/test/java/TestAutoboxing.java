import org.junit.jupiter.api.Test;

public class TestAutoboxing extends HiTest {
	String[] operations = {"+", "-", "*", "/", "%", "&", "|", "^", "<<", ">>", ">>>"};

	@Test
	public void testByte() {
		// create autobox object
		assertSuccess("Byte a = new Byte((byte)127); assert a == 127;");
		assertSuccess("var a = new Byte((byte)127); assert a == 127;");

		// primitive => autobox object
		assertSuccess("Byte a = (byte)127; assert a == 127;");
		assertSuccess("Byte a = 127; assert a == 127;");
		assertSuccess("Byte a = (short)127; assert a == 127;");
		assertFailCompile("Byte a = 128;");
		assertSuccess("Byte a = (byte)127; assert a != null;");
		assertSuccess("Byte a = 127; assert a.toString().equals(\"127\");");
		assertSuccess("Byte a = 127; assert a instanceof Byte;");
		assertSuccess("Byte a = 1; assert a instanceof Number;");
		assertSuccess("byte a = 127; Byte b = a; assert b == a; assert b == 127;");
		assertSuccess("byte a = 127; Byte b = 127; assert b == a;");
		assertFailCompile("Byte a = 128;");
		assertFailCompile("Byte a = 1L;");
		assertFailCompile("Byte a = (short)128;");
		assertFailCompile("Byte a = 1.1;");
		assertFailCompile("Byte a = 1f;");
		assertFailCompile("Byte a = true;");

		// autobox object => primitive
		assertSuccess("byte a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("int a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("short a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("long a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("float a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertSuccess("double a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
		assertFail("Byte a = null; byte b = a;");

		// operations
		assertSuccess("Byte a = 1; Byte b = 2; Byte c = 3; assert a + b == c;");
		assertSuccess("Byte a = 1; Byte b = 1; assert a != b;");
		for (String operation : operations) {
			String B1 = "new Byte((byte)63)";
			String b1 = "(byte)63";
			String B2 = "new Byte((byte)3)";
			String b2 = "(byte)3";
			assertSuccess("assert (" + B1 + " " + operation + " " + b2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
			assertSuccess("assert (" + b1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
			assertSuccess("assert (" + B1 + " " + operation + " " + B2 + ") == (" + b1 + " " + operation + " " + b2 + ");");
		}

		assertSuccess("Byte a = 1; a++; assert a == 2;");
		assertSuccess("Byte a = 1; ++a; assert a == 2;");
		assertSuccess("Byte a = 1; a--; assert a == 0;");
		assertSuccess("Byte a = 1; --a; assert a == 0;");
		for (String operation : operations) {
			assertFailCompile("Byte a = 32; a " + operation + "= 1;");
		}

		// class fields
		assertSuccess("class C{Byte a; C(Byte a) {this.a = a;}} assert new C((byte)127).a == 127;");

		// methods + return
		assertSuccess("class C{byte set(Byte a){return a;}} assert new C().set(new Byte((byte)123)) == new Byte((byte)123) : \"assert 2\";");
		assertSuccess("class C{Byte set(byte a){return a;}} assert new C().set(new Byte((byte)123)) == 123;");
		assertSuccess("class C{void set(byte... a){assert a[1] == 127;}} new C().set(new Byte((byte)0), new Byte((byte)127));");
		assertSuccess("class C{C(byte... a){assert a[0] == 0; assert a[1] == 127; assert a[2] == -1;}} new C(new Byte((byte)0), new Byte((byte)127), (byte)-1);");
		assertFail("class C{C(byte... a){}} new C(new Byte((byte)0), new Byte((byte)127), null);");

		// constructors
		assertSuccess("class C{byte set(Byte a){assert a == 123; return a;}} assert new C().set((byte)123) == 123;");
		assertSuccess("class C{Byte set(byte a){return a;}} assert new C().set((byte)123) == 123;");
		assertSuccess("class C{C(Object a){assert a instanceof Byte;}} new C((byte)123);");
		assertSuccess("class C{C(Byte... a){assert a[0] instanceof Byte; assert a[1] instanceof Byte; assert a[1] == 127; assert a[2] == null; assert a[3] == -1;}} new C((byte)0, (byte)127, null, new Byte((byte)-1));");
		assertSuccess("class C{C(Object... a){assert a[0] instanceof Byte; assert a[1] instanceof Byte; assert (Byte)a[1] == 127;}} new C((byte)0, (byte)127);");

		// arrays
		assertSuccess("Byte[] a = new Byte[3]; a[0] = (byte)1; a[1] = 127; assert a[0] == 1; assert a[1] == 127; assert a[2] == null;");
		assertSuccess("byte[] a = new byte[2]; a[1] = new Byte((byte)127); assert a[1] == 127;");
	}

	@Test
	public void testShort() {
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
	}

	@Test
	public void testCharacter() {
	}
}