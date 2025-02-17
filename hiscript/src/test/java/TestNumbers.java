import org.junit.jupiter.api.Test;

public class TestNumbers extends HiTest {
	@Test
	public void testNumbersDeclarations() {
		assertSuccessSerialize("int a = 1; assert a == 1;");
		assertSuccessSerialize("int a = +1; assert a == 1;");
		assertSuccessSerialize("int a = -1; assert a == -1;");
		assertSuccessSerialize("int a = --1; assert a == 1;");
		assertSuccessSerialize("int a = ---1; assert a == -1;");
		assertCondition("int a = + -  + -+ -1;", "a == -1", "int");
		assertFailCompile("int a = 1.;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 1.0;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 10.0e-1;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 0.0e-1;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 0.01e2;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 1l;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("int a = 1L;", //
				"incompatible types: long cannot be converted to int");
		assertFailCompile("int a = 1f;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int a = 1F;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int a = 1.0f;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int a = 1.0F;", //
				"incompatible types: float cannot be converted to int");
		assertFailCompile("int a = 1d;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 1D;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 1.0d;", //
				"incompatible types: double cannot be converted to int");
		assertFailCompile("int a = 1.0D;", //
				"incompatible types: double cannot be converted to int");
		assertCondition("int a = (byte)1;", "a == 1l", "int");
		assertCondition("int a = (short)1;", "a == (long)1", "int");
		assertCondition("int a = (char)1;", "a == 1", "int");
		assertCondition("int a = 'a';", "a == (int)'a'", "int");
		assertFailCompile("int a = 1e;", //
				"expression expected");
		assertFailCompile("int a = 10_000_000_000;", //
				"integer number too large");
		assertFailCompile("int a = 0xfffffffff;", //
				"integer number too large");
		assertFailCompile("int a = -1.0e;", //
				"expression expected");

		assertCondition("long a = 1;", "a == 1", "long");
		assertCondition("long a = -1;", "a == -1", "long");
		assertFailCompile("long a = 1.;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 1.0;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 10.0e-1;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 0.0e-1;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 0.01e2;", //
				"incompatible types: double cannot be converted to long");
		assertCondition("long a = 1l;", "a == 1", "long");
		assertCondition("long a = 1L;", "a == 1", "long");
		assertFailCompile("long a = 1f;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long a = 1F;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long a = 1.0f;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long a = 1.0F;", //
				"incompatible types: float cannot be converted to long");
		assertFailCompile("long a = 1d;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 1D;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 1.0d;", //
				"incompatible types: double cannot be converted to long");
		assertFailCompile("long a = 1.0D;", //
				"incompatible types: double cannot be converted to long");
		assertCondition("long a = -+-(byte)1;", "a == 1", "long");
		assertCondition("long a = (short)1;", "a == 1", "long");
		assertCondition("long a = (char)1;", "a == 1", "long");
		assertCondition("long a = 'a';", "a == (long)'a'", "long");
		assertFailCompile("long a = 0xABCDEFabcdefabcdefabcdef1234567890L;", //
				"long number too large");
		assertFailCompile("long a = 1000000000000000000000000000000000000000000000L;", //
				"long number too large");
		assertFailCompile("long a = 0xQWEL;", //
				"hexadecimal numbers must contain at least one hexadecimal digit");

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
		assertFailCompile("float a = 1.0;", //
				"incompatible types: double cannot be converted to float");
		assertFailCompile("float a = 1d;", //
				"incompatible types: double cannot be converted to float");
		assertFailCompile("float a = 1D;", //
				"incompatible types: double cannot be converted to float");
		assertFailCompile("float a = 1.0d;", //
				"incompatible types: double cannot be converted to float");
		assertFailCompile("float a = 1.0D;", //
				"incompatible types: double cannot be converted to float");
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
		assertFailCompile("byte a = 1.;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1.0;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 10.0e-1;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 0.0e-1;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 0.01e2;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1d;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1D;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1.0d;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1.0D;", //
				"incompatible types: double cannot be converted to byte");
		assertFailCompile("byte a = 1l;", //
				"incompatible types: long cannot be converted to byte");
		assertFailCompile("byte a = 1L;", //
				"incompatible types: long cannot be converted to byte");
		assertFailCompile("byte a = 1f;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("byte a = 1F;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("byte a = 1.0f;", //
				"incompatible types: float cannot be converted to byte");
		assertFailCompile("byte a = 1.0F;", //
				"incompatible types: float cannot be converted to byte");
		assertCondition("byte a = (byte)1;", "a == 1l", "byte");
		assertCondition("byte a = (short)1;", "a == 1", "byte");
		assertCondition("byte a = (char)1;", "a == 1", "byte");
		assertSuccessSerialize("byte a = 'a'; assert a == 'a';");

		assertCondition("short a = 127;", "a == 127", "short");
		assertCondition("short a = -+-+-111;", "a == -111", "short");
		assertCondition("short a = -128;", "a == -128", "short");
		assertFailCompile("short a = 1.;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1.0;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 10.0e-1;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 0.0e-1;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 0.01e2;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1d;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1D;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1.0d;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1.0D;", //
				"incompatible types: double cannot be converted to short");
		assertFailCompile("short a = 1l;", //
				"incompatible types: long cannot be converted to short");
		assertFailCompile("short a = 1L;", //
				"incompatible types: long cannot be converted to short");
		assertFailCompile("short a = 1f;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("short a = 1F;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("short a = 1.0f;", //
				"incompatible types: float cannot be converted to short");
		assertFailCompile("short a = 1.0F;", //
				"incompatible types: float cannot be converted to short");
		assertCondition("short a = (byte)1;", "a == 1l", "short");
		assertCondition("byte a = (short)1;", "a == 1", "short");
		assertCondition("byte a = (char)1;", "a == 1", "short");
		assertSuccessSerialize("byte a = 'a'; assert a == 'a';");

		for (String t : new String[] {"byte", "short", "int", "long", "float", "double", "boolean"}) {
			for (char c = 0; c < 255; c++) {
				if (!Character.isDigit(c) && !Character.isLetter(c) && c != ';') {
					String script = t + " a = " + c + ";";
					switch (c) {
						case '"':
							assertFailCompile(script, //
									"'\"' is expected");
							break;
						case '\'':
							assertFailCompile(script, //
									"' is expected");
							break;
						case '$':
						case '_':
							assertFailCompile(script, //
									"cannot resolve symbol '" + c + "'");
							break;
						case '@':
							assertFailCompile(script, //
									"unexpected token");
							break;
						default:
							assertFailCompile(script, //
									"expression expected");
							break;
					}
				}
			}
		}

		// var
		assertFailCompile("!true;", //
				"not a statement");
		assertFailCompile("1++;", //
				"variable expected");
		assertFailCompile("1--;", //
				"variable expected");
		assertFailCompile("++1;", //
				"not a statement");
		assertFailCompile("--1;", //
				"not a statement");
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
		assertFailCompile("long a = 0x0x;", //
				"not a statement");

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
		assertFailCompile("char c = 'a;", //
				"' is expected");
		assertFailCompile("char c = '';", //
				"empty character literal");
		assertFailCompile("char c = 'ab';", //
				"too many characters in character literal");
		assertFailCompile("char c = \"a\";", //
				"incompatible types: String cannot be converted to char");
		assertFailCompile("char c = 0L;", //
				"incompatible types: long cannot be converted to char");
		assertFailCompile("char c = 10f;", //
				"incompatible types: float cannot be converted to char");
		assertFailCompile("char c = 0.0;", //
				"incompatible types: double cannot be converted to char");
		assertFailCompile("char c = 100000000;", //
				"incompatible types: int cannot be converted to char");
	}

	@Test
	public void testNumbersLimits() {
		assertSuccessSerialize("byte a = " + Byte.MAX_VALUE + ";");
		assertSuccessSerialize("byte a = " + Byte.MIN_VALUE + ";");
		assertFailCompile("byte a = " + (Byte.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to byte");
		assertFailCompile("byte a = " + (Byte.MIN_VALUE - 1) + ";", //
				"incompatible types: int cannot be converted to byte");

		assertFailCompile("short a = " + (Short.MAX_VALUE + 1) + ";", //
				"incompatible types: int cannot be converted to short");
		assertFailCompile("short a = " + (Short.MIN_VALUE - 1) + ";", //
				"incompatible types: int cannot be converted to short");

		assertFailCompile("int a = " + (Integer.MAX_VALUE + 1L) + ";", //
				"integer number too large");
		assertFailCompile("int a = " + (Integer.MIN_VALUE - 1L) + ";", //
				"integer number too large");

		assertFailCompile("long a = " + Long.MAX_VALUE + "0;", //
				"integer number too large");
		assertFailCompile("long a = " + Long.MIN_VALUE + "0;", //
				"integer number too large");

		assertFailCompile("float a = 1" + Float.MAX_VALUE + ";", //
				"incompatible types: double cannot be converted to float");
		assertFailCompile("float a = 1" + Float.MIN_VALUE + ";", //
				"incompatible types: double cannot be converted to float");
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

	@Test
	public void testNumbersExpressionLimits() {
		assertSuccessSerialize("byte a = 20 * 6 + 7; assert a == 127;");
		assertFailCompile("byte a = 127 + 1;", //
				"incompatible types: int cannot be converted to byte");
		assertFailCompile("byte a = 20 * 20;", //
				"incompatible types: int cannot be converted to byte");
		assertFailCompile("byte a = -128 - 1;", //
				"incompatible types: int cannot be converted to byte");
		assertFailCompile("byte a = -20 * 20;", //
				"incompatible types: int cannot be converted to byte");

		assertFailCompile("short a = 32767 + 1;", //
				"incompatible types: int cannot be converted to short");
		assertFailCompile("short a = -32768 - 1;", //
				"incompatible types: int cannot be converted to short");
		assertFailCompile("short a = 200 * 200;", //
				"incompatible types: int cannot be converted to short");
		assertFailCompile("short a = -200 * 200;", //
				"incompatible types: int cannot be converted to short");

		assertSuccessSerialize("int a = 1_000_000 * 1_000_000 * 1_000_000;");
		assertSuccessSerialize("int a = -1_000_000 * 1_000_000 * 1_000_000;");
	}

	@Test
	public void testNumbersFormats() {
		// int numbers
		assertFailCompile("int x = _1;", //
				"cannot resolve symbol '_1'");
		assertFailCompile("int x = 1_;", //
				"illegal underscore");
		assertFailCompile("int x = 1__________;", //
				"illegal underscore");
		// hex numbers
		assertSuccessSerialize("int x = -0xf_f; assert -x == 255;");
		assertFailCompile("int x = 0xFF_;", //
				"illegal underscore");
		assertFailCompile("int x = 0x_FF;", //
				"illegal underscore");
		assertFailCompile("long x = 0xFF__________L;", //
				"illegal underscore");
		// binary numbers
		assertSuccessSerialize("int x = 0b101; assert x == 5;");
		assertSuccessSerialize("long x = 0b101L; assert x == 5L;");
		assertSuccessSerialize("int x = -0b101; assert -x == 5;");
		assertFailCompile("int x = 0b10000000000_0000000000_0000000000_00;", // 32 bits
				"integer number too large");
		assertFailCompile("long x = 0b10000000000_0000000000_0000000000_0000000000_0000000000_0000000000_000L;", // 64 bits
				"long number too large");
		assertFailCompile("int x = 0b2;", //
				"binary numbers must contain at least one binary digit");
		assertFailCompile("int x = 0b111_;", //
				"illegal underscore");
		assertFailCompile("int x = 0b_111;", //
				"illegal underscore");
		// octal numbers
		assertSuccessSerialize("int x = 010; assert x == 8;");
		assertSuccessSerialize("long x = 010L; assert x == 8L;");
		assertSuccessSerialize("int x = -010; assert -x == 8;");
		assertFailCompile("int x = 010000000000_0;", // 11 octals = 11*3=33 bits > 32 bits (max)
				"integer number too large");
		assertFailCompile("long x = 010000000000_0000000000_00L;", // 22 octals = 22*3=66 bits > 64 bits (max)
				"long number too large");
		assertSuccessSerialize("float x = 077f; assert x == 77;"); // non octal
		assertSuccessSerialize("double x = 077d; assert x == 77;"); // non octal
		assertSuccessSerialize("double x = 077e1; assert x == 770;"); // non octal
		assertFailCompile("int x = 077_;", //
				"illegal underscore");
		// numbers
		assertFailCompile("float x = 10e200f;", //
				"float number too large");
		assertFailCompile("float x = 10e1000000000000000000000000000000000000000000000000000000000000f;", //
				"float number too large");
		assertFailCompile("double x = 10e1000;", //
				"double number too large");
	}
}
