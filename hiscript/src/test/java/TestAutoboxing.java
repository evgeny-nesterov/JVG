import org.junit.jupiter.api.Test;

public class TestAutoboxing extends HiTest {
	@Test
	public void testByte() {
//		// create autobox object
//		assertSuccess("Byte a = new Byte((byte)127); assert a == 127;");
//		assertSuccess("var a = new Byte((byte)127); assert a == 127;");
//
//		// primitive => autobox object
//		assertSuccess("Byte a = (byte)127; assert a == 127;");
//		assertSuccess("Byte a = 127; assert a == 127;");
//		assertFailCompile("Byte a = 128;");
//		assertSuccess("Byte a = (byte)127; assert a != null;");
//		assertSuccess("Byte a = 127; assert a.toString().equals(\"127\");");
//		assertSuccess("Byte a = 127; assert a instanceof Byte;");
//		assertSuccess("Byte a = 1; assert a instanceof Number;");
//		assertSuccess("byte a = 127; Byte b = a; assert b == a; assert b == 127;");
//		assertSuccess("byte a = 127; Byte b = 127; assert b == a;");
//
//		// autobox object => primitive
//		assertSuccess("byte a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//		assertSuccess("int a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//		assertSuccess("short a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//		assertSuccess("long a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//		assertSuccess("float a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//		assertSuccess("double a = new Byte((byte)127); assert a == 127; a = new Byte((byte)-1); assert a == -1;");
//
//		// operations
//		assertSuccess("assert new Byte((byte)1) + 10 == 11;");
//		assertSuccess("assert 1 + new Byte((byte)10) == 11;");
//		assertSuccess("assert new Byte((byte)1) + new Byte((byte)10) == 11;");
//
//		assertSuccess("assert new Byte((byte)11) - 10 == 1;");
//		assertSuccess("assert 11 - new Byte((byte)10) == 1;");
//		assertSuccess("assert new Byte((byte)11) - new Byte((byte)10) == 1;");
//
//		assertSuccess("assert new Byte((byte)2) * 5 == 10;");
//		assertSuccess("assert 2 * new Byte((byte)5) == 10;");
//		assertSuccess("assert new Byte((byte)2) * new Byte((byte)5) == 10;");
//
//		assertSuccess("assert new Byte((byte)10) / 5 == 2;");
//		assertSuccess("assert 11 / new Byte((byte)5) == 2;");
//		assertSuccess("assert new Byte((byte)10) / new Byte((byte)5) == 2;");

		assertSuccess("Byte a = 1; a++; assert a == 2;");
		assertSuccess("Byte a = 1; a--; assert a == 0;");
		assertSuccess("Byte a = 1; a += 1; assert a == 2;");
		assertSuccess("Byte a = 1; a -= 1; assert a == 0;");
		assertSuccess("Byte a = 10; a *= 2; assert a == 20;");
		assertSuccess("Byte a = 10; a /= 2; assert a == 5;");
		// TODO

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