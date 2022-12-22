import org.junit.jupiter.api.Test;

public class TestNumbers extends HiTest {
	@Test
	public void testNumbersDeclarations() {
		assertCondition("int a = 1;", "a == 1", "int");
		assertCondition("int a = + -  + -+ -1;", "a == -1", "int");
		assertFail("int a = 1.;", "int");
		assertFail("int a = 1.0;", "int");
		assertFail("int a = 10.0e-1;", "int");
		assertFail("int a = 0.0e-1;", "int");
		assertFail("int a = 0.01e2;", "int");
		assertFail("int a = 1l;", "int");
		assertFail("int a = 1L;", "int");
		assertFail("int a = 1f;", "int");
		assertFail("int a = 1F;", "int");
		assertFail("int a = 1.0f;", "int");
		assertFail("int a = 1.0F;", "int");
		assertFail("int a = 1d;", "int");
		assertFail("int a = 1D;", "int");
		assertFail("int a = 1.0d;", "int");
		assertFail("int a = 1.0D;", "int");
		assertCondition("int a = (byte)1;", "a == 1l", "int");
		assertCondition("int a = (short)1;", "a == (long)1", "int");
		assertCondition("int a = (char)1;", "a == 1", "int");
		assertCondition("int a = 'a';", "a == (int)'a'", "int");
		assertFail("int a = 1e;");

		assertCondition("long a = 1;", "a == 1", "long");
		assertFail("long a = 1.;", "long");
		assertFail("long a = 1.0;", "long");
		assertFail("long a = 10.0e-1;", "long");
		assertFail("long a = 0.0e-1;", "long");
		assertFail("long a = 0.01e2;", "long");
		assertCondition("long a = 1l;", "a == 1", "long");
		assertCondition("long a = 1L;", "a == 1", "long");
		assertFail("long a = 1f;", "long");
		assertFail("long a = 1F;", "long");
		assertFail("long a = 1.0f;", "long");
		assertFail("long a = 1.0F;", "long");
		assertFail("long a = 1d;", "long");
		assertFail("long a = 1D;", "long");
		assertFail("long a = 1.0d;", "long");
		assertFail("long a = 1.0D;", "long");
		assertCondition("long a = -+-(byte)1;", "a == 1", "long");
		assertCondition("long a = (short)1;", "a == 1", "long");
		assertCondition("long a = (char)1;", "a == 1", "long");
		assertCondition("long a = 'a';", "a == (long)'a'", "long");

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
		assertFail("float a = 1.0;", "float");
		assertFail("float a = 1d;", "float");
		assertFail("float a = 1D;", "float");
		assertFail("float a = 1.0d;", "float");
		assertFail("float a = 1.0D;", "float");
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
		assertFail("byte a = 1.;", "byte");
		assertFail("byte a = 1.0;", "byte");
		assertFail("byte a = 10.0e-1;", "byte");
		assertFail("byte a = 0.0e-1;", "byte");
		assertFail("byte a = 0.01e2;", "byte");
		assertFail("byte a = 1l;", "byte");
		assertFail("byte a = 1L;", "byte");
		assertFail("byte a = 1f;", "byte");
		assertFail("byte a = 1F;", "byte");
		assertFail("byte a = 1.0f;", "byte");
		assertFail("byte a = 1.0F;", "byte");
		assertFail("byte a = 1d;", "byte");
		assertFail("byte a = 1D;", "byte");
		assertFail("byte a = 1.0d;", "byte");
		assertFail("byte a = 1.0D;", "byte");
		assertCondition("byte a = (byte)1;", "a == 1l", "byte");
		assertCondition("byte a = (short)1;", "a == 1", "byte");
		assertCondition("byte a = (char)1;", "a == 1", "byte");
		assertSuccessSerialize("byte a = 'a'; assert a == 'a';");
	}

	@Test
	public void testNumbersLimits() {
		assertSuccessSerialize("byte a = " + Byte.MAX_VALUE + ";");
		assertSuccessSerialize("byte a = " + Byte.MIN_VALUE + ";");
		assertFail("byte a = " + (Byte.MAX_VALUE + 1) + ";");
		assertFail("byte a = " + (Byte.MIN_VALUE - 1) + ";");

		assertFail("short a = " + (Short.MAX_VALUE + 1) + ";");
		assertFail("short a = " + (Short.MIN_VALUE - 1) + ";");

		assertFail("int a = " + (Integer.MAX_VALUE + 1) + ";");
		assertFail("int a = " + (Integer.MIN_VALUE - 1l) + ";");

		assertFail("long a = " + Long.MAX_VALUE + "0;");
		assertFail("long a = " + Long.MIN_VALUE + "0;");

		assertFail("float a = 1" + Float.MAX_VALUE + ";");
		assertFail("float a = 1" + Float.MIN_VALUE + ";");
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
