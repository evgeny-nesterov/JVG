import org.junit.jupiter.api.Test;

public class TestNumbers extends HiTest {
	@Test
	public void testNumbersDeclarations() {
		assertSuccess("int a = 1; assert a == 1;");
		assertSuccess("int a = +1; assert a == 1;");
		assertSuccess("int a = -1; assert a == -1;");
		assertSuccess("int a = --1; assert a == 1;");
		assertSuccess("int a = ---1; assert a == -1;");
		assertCondition("int a = + -  + -+ -1;", "a == -1", "int");
		assertFailCompile("int a = 1.;");
		assertFailCompile("int a = 1.0;");
		assertFailCompile("int a = 10.0e-1;");
		assertFailCompile("int a = 0.0e-1;");
		assertFailCompile("int a = 0.01e2;");
		assertFailCompile("int a = 1l;");
		assertFailCompile("int a = 1L;");
		assertFailCompile("int a = 1f;");
		assertFailCompile("int a = 1F;");
		assertFailCompile("int a = 1.0f;");
		assertFailCompile("int a = 1.0F;");
		assertFailCompile("int a = 1d;");
		assertFailCompile("int a = 1D;");
		assertFailCompile("int a = 1.0d;");
		assertFailCompile("int a = 1.0D;");
		assertCondition("int a = (byte)1;", "a == 1l", "int");
		assertCondition("int a = (short)1;", "a == (long)1", "int");
		assertCondition("int a = (char)1;", "a == 1", "int");
		assertCondition("int a = 'a';", "a == (int)'a'", "int");
		assertFailCompile("int a = 1e;");
		assertFailCompile("int a = 10_000_000_000;");
		assertFailCompile("int a = 0xfffffffff;");
		assertFailCompile("int a = -1.0e;");

		assertCondition("long a = 1;", "a == 1", "long");
		assertCondition("long a = -1;", "a == -1", "long");
		assertFailCompile("long a = 1.;");
		assertFailCompile("long a = 1.0;");
		assertFailCompile("long a = 10.0e-1;");
		assertFailCompile("long a = 0.0e-1;");
		assertFailCompile("long a = 0.01e2;");
		assertCondition("long a = 1l;", "a == 1", "long");
		assertCondition("long a = 1L;", "a == 1", "long");
		assertFailCompile("long a = 1f;");
		assertFailCompile("long a = 1F;");
		assertFailCompile("long a = 1.0f;");
		assertFailCompile("long a = 1.0F;");
		assertFailCompile("long a = 1d;");
		assertFailCompile("long a = 1D;");
		assertFailCompile("long a = 1.0d;");
		assertFailCompile("long a = 1.0D;");
		assertCondition("long a = -+-(byte)1;", "a == 1", "long");
		assertCondition("long a = (short)1;", "a == 1", "long");
		assertCondition("long a = (char)1;", "a == 1", "long");
		assertCondition("long a = 'a';", "a == (long)'a'", "long");
		assertFailCompile("long a = 0xABCDEFabcdefabcdefabcdef1234567890L;");
		assertFailCompile("long a = 1000000000000000000000000000000000000000000000L;");
		assertFailCompile("long a = 0xQWEL;");

		assertCondition("double a = 1;", "a == 1", "double");
		assertCondition("double a = 1.;", "a == 1", "double");
		assertCondition("double a = .1;", "a == 0.1", "double");
		assertCondition("double a = 1.0;", "a == 1.0", "double");
		assertCondition("double a = 10.0e-1;", "a == 1", "double");
		assertCondition("double a = 0.0e-1;", "a == 0", "double");
		assertCondition("double a = 0.01e2;", "a == 1", "double");
		assertCondition("double a = -+-+-.01e+2;", "a == -1", "double");
		assertCondition("double a = 1l;", "a == 1l", "double");
		assertCondition("double a = 1L;", "a == 1L", "double");
		assertCondition("double a = 1f;", "a == 1f", "double");
		assertCondition("double a = 1F;", "a == 1F", "double");
		assertCondition("double a = 1.0f;", "a == 1.0f", "double");
		assertCondition("double a = 1.0F;", "a == 1.0F", "double");
		assertCondition("double a = 1d;", "a == 1d", "double");
		assertCondition("double a = 1D;", "a == 1D", "double");
		assertCondition("double a = 1.0d;", "a == 1d", "double");
		assertCondition("double a = 1.0D;", "a == 1D", "double");
		assertCondition("double a = (byte)1;", "a == 1", "double");
		assertCondition("double a = (short)1;", "a == 1", "double");
		assertCondition("double a = (char)1;", "a == 1", "double");
		assertCondition("double a = 'a';", "a == (double)'a'", "double");

		assertCondition("float a = 1;", "a == 1", "float");
		assertCondition("float a = 10.0e-1f;", "a == 1", "float");
		assertCondition("float a = 0.0e-1f;", "a == 0", "float");
		assertCondition("float a = 0.01e2f;", "a == 1", "float");
		assertFailCompile("float a = 1.0;");
		assertFailCompile("float a = 1d;");
		assertFailCompile("float a = 1D;");
		assertFailCompile("float a = 1.0d;");
		assertFailCompile("float a = 1.0D;");
		assertCondition("float a = 1l;", "a == (byte)1", "float");
		assertCondition("float a = 1L;", "a == 1L", "float");
		assertCondition("float a = 1f;", "a == 1f", "float");
		assertCondition("float a = 1F;", "a == (short)1F", "float");
		assertCondition("float a = 1.0f;", "a == (double)1.0f", "float");
		assertCondition("float a = 1.0F;", "a == 1.0F", "float");
		assertCondition("float a = (byte)1;", "a == 1d", "float");
		assertCondition("float a = (short)1;", "a == 1F", "float");
		assertCondition("float a = (char)1;", "a == 1f", "float");
		assertCondition("float a = 'a';", "a == (float)'a'", "float");

		assertCondition("byte a = 127;", "a == 127", "byte");
		assertCondition("byte a = -+-+-111;", "a == -111", "byte");
		assertCondition("byte a = -128;", "a == -128", "byte");
		assertFailCompile("byte a = 1.;");
		assertFailCompile("byte a = 1.0;");
		assertFailCompile("byte a = 10.0e-1;");
		assertFailCompile("byte a = 0.0e-1;");
		assertFailCompile("byte a = 0.01e2;");
		assertFailCompile("byte a = 1l;");
		assertFailCompile("byte a = 1L;");
		assertFailCompile("byte a = 1f;");
		assertFailCompile("byte a = 1F;");
		assertFailCompile("byte a = 1.0f;");
		assertFailCompile("byte a = 1.0F;");
		assertFailCompile("byte a = 1d;");
		assertFailCompile("byte a = 1D;");
		assertFailCompile("byte a = 1.0d;");
		assertFailCompile("byte a = 1.0D;");
		assertCondition("byte a = (byte)1;", "a == 1l", "byte");
		assertCondition("byte a = (short)1;", "a == 1", "byte");
		assertCondition("byte a = (char)1;", "a == 1", "byte");
		assertSuccessSerialize("byte a = 'a'; assert a == 'a';");

		assertCondition("short a = 127;", "a == 127", "short");
		assertCondition("short a = -+-+-111;", "a == -111", "short");
		assertCondition("short a = -128;", "a == -128", "short");
		assertFailCompile("short a = 1.;");
		assertFailCompile("short a = 1.0;");
		assertFailCompile("short a = 10.0e-1;");
		assertFailCompile("short a = 0.0e-1;");
		assertFailCompile("short a = 0.01e2;");
		assertFailCompile("short a = 1l;");
		assertFailCompile("short a = 1L;");
		assertFailCompile("short a = 1f;");
		assertFailCompile("short a = 1F;");
		assertFailCompile("short a = 1.0f;");
		assertFailCompile("short a = 1.0F;");
		assertFailCompile("short a = 1d;");
		assertFailCompile("short a = 1D;");
		assertFailCompile("short a = 1.0d;");
		assertFailCompile("short a = 1.0D;");
		assertCondition("short a = (byte)1;", "a == 1l", "short");
		assertCondition("byte a = (short)1;", "a == 1", "short");
		assertCondition("byte a = (char)1;", "a == 1", "short");
		assertSuccessSerialize("byte a = 'a'; assert a == 'a';");

		for (String t : new String[] {"byte", "short", "int", "long", "float", "double", "boolean"}) {
			for (char c = 0; c < 255; c++) {
				if (!Character.isDigit(c) && c != ';') {
					assertFailCompile(t + " a = " + c + ";");
				}
			}
		}
	}

	@Test
	public void testHex() {
		assertSuccessSerialize("int a = 0x1; assert a == 1;");
		assertSuccessSerialize("int a = 0x01; assert a == 1;");
		assertSuccessSerialize("int a = 0x00000000000000000000000000001; assert a == 1;");
		assertSuccessSerialize("int a = 0x0f; assert a == 15;");
		assertSuccessSerialize("int a = 0x0F; assert a == 15;");
		assertSuccessSerialize("long a = 0xffL; assert a == 255;");
		assertSuccessSerialize("long a = 0x01L; assert a == 1L;");
		assertFailCompile("long a = 0x0x;");

		assertSuccessSerialize("int a = 0x9 + 0x6; assert a == 0x0000f;");
		assertSuccessSerialize("int a = 0x10<<1; assert a == 0x20;");
		assertSuccessSerialize("int a = 0x10<<4; assert a == 0x100;");
		assertSuccessSerialize("int a = 0x10<<4>>4; assert a == 16;");
		assertSuccessSerialize("int a = 0x10<<4<<4; assert a == 0x1000;");
	}

	@Test
	public void testChars() {
		assertSuccessSerialize("char c = '\\u0030'; assert c == '0'; assert c == 48;");
		assertSuccessSerialize("char c = '\\uFFEE'; assert c == '￮'; assert c == 65518;");
		assertSuccessSerialize("char c = 'a' + 1; assert c == 'b';");
		assertSuccessSerialize("char c = 50; assert c == 50;");
		assertSuccessSerialize("char c = (byte)50; assert c == 50;");
		assertFailCompile("char c = ' a';");
		assertFailCompile("char c = \"a\";");
		assertFailCompile("char c = 0L;");
		assertFailCompile("char c = 10f;");
		assertFailCompile("char c = 0.0;");
		assertFailCompile("char c = 100000000;");
	}

	@Test
	public void testNumbersLimits() {
		assertSuccessSerialize("byte a = " + Byte.MAX_VALUE + ";");
		assertSuccessSerialize("byte a = " + Byte.MIN_VALUE + ";");
		assertFailCompile("byte a = " + (Byte.MAX_VALUE + 1) + ";");
		assertFailCompile("byte a = " + (Byte.MIN_VALUE - 1) + ";");

		assertFailCompile("short a = " + (Short.MAX_VALUE + 1) + ";");
		assertFailCompile("short a = " + (Short.MIN_VALUE - 1) + ";");

		assertFailCompile("int a = " + (Integer.MAX_VALUE + 1L) + ";");
		assertFailCompile("int a = " + (Integer.MIN_VALUE - 1L) + ";");

		assertFailCompile("long a = " + Long.MAX_VALUE + "0;");
		assertFailCompile("long a = " + Long.MIN_VALUE + "0;");

		assertFailCompile("float a = 1" + Float.MAX_VALUE + ";");
		assertFailCompile("float a = 1" + Float.MIN_VALUE + ";");
	}

	@Test
	public void testNumbersComparison() {
		assertSuccessSerialize("assert 1>0;");
		assertSuccessSerialize("assert 0<1;");
		assertSuccessSerialize("assert 0<=0;");
		assertSuccessSerialize("assert 1>=1;");
		assertSuccessSerialize("assert 1==1;");
		assertSuccessSerialize("assert 1+1>0+0;");
		assertSuccessSerialize("assert 1+1<2*2;");
	}
}
