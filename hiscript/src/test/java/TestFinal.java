import org.junit.jupiter.api.Test;

public class TestFinal extends HiTest {
	@Test
	public void testField() {
		String[] primitiveNumberTypes = {"byte", "short", "char", "int", "long", "float", "double"};
		boolean[] intNumbers = {true, true, true, true, true, false, false};
		boolean[] allowEquatePlus_ = {false, false, false, true, true, true, true};
		for (int i = 0; i < primitiveNumberTypes.length; i++) {
			String t = primitiveNumberTypes[i];
			boolean intNumber = intNumbers[i];
			boolean allowEquatePlus = allowEquatePlus_[i];

			assertSuccessSerialize("final " + t + " x; x = 1; assert x == 1;");
			assertSuccessSerialize("final String x; x = \"a\"; assert x.equals(\"a\");");
			assertSuccessSerialize("final String x; x = null; assert x == null;");
			assertSuccessSerialize("final " + t + " x = 1; class A{" + t + " y; A(){this.y = x;}} new A();");
			assertSuccessSerialize("final " + t + " x = 1; class A{" + t + " y = x;} new A();");
			assertSuccessSerialize("try{" + t + " x = 0;} catch(final Exception e){}");
			assertSuccessSerialize("class A{final " + t + " x = 1; {" + t + " y = (" + t + ")(x + 1);}}");
			assertSuccessSerialize("class A{final static " + t + " x = 1; static{" + t + " y = (" + t + ")(x + 1L);}}");
			assertSuccessSerialize("class A{" + t + " get(final " + t + " x){final " + t + " y = (" + t + ")(x + 1.0); return y;}}");

			assertFailCompile("final " + t + " x = 1; x = 1;", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x; x = 1; x = 2;", "cannot assign value to final variable");
			assertFailCompile("final String x = \"\"; x = null;", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x = 1; " + t + " y = 2; x = y;", "cannot assign value to final variable");

			assertFailCompile("class A{final " + t + " x; {x = 1;}}", "variable 'x' might not have been initialized");
			assertFailCompile("class A{final " + t + " x = 1; {x = 1;}}", "cannot assign value to final variable");
			assertFailCompile("class A{final static " + t + " x = 1; static{x = 1;}}", "cannot assign value to final variable");
			assertFailCompile("class A{void a(final " + t + " a){a = 1;}}", "cannot assign value to final variable");
			assertFailCompile("class A{A(final " + t + " a){a = 1;}}", "cannot assign value to final variable");
			assertFailCompile("try{" + t + " x = 0;} catch(final Exception e){e = null;}", "cannot assign value to final variable");
			assertFailCompile("class A{void a(final " + t + "... a){a = null;}}", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x = 1; class A{{x = 1;}}", "cannot assign value to final variable");

			// number operations which changes value
			if (allowEquatePlus) {
				assertFailCompile("final " + t + " x; x += 1;", "variable 'x' is not initialized");
				assertFailCompile("final " + t + " x; x -= 1;", "variable 'x' is not initialized");
				assertFailCompile("final " + t + " x; x %= 1;", "variable 'x' is not initialized");

				assertFailCompile("final " + t + " x = 1; x += 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x -= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x %= 1;", "cannot assign value to final variable");
			}
			if (intNumber) {
				assertFailCompile("final " + t + " x = 1; x |= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x &= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x ^= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x ~= 1;");
				assertFailCompile("final " + t + " x = 1; x >>= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x <<= 1;", "cannot assign value to final variable");
				assertFailCompile("final " + t + " x = 1; x >>>= 1;", "cannot assign value to final variable");
			}
			assertFailCompile("final " + t + " x = 1; x++;", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x = 1; ++x;", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x = 1; x--;", "cannot assign value to final variable");
			assertFailCompile("final " + t + " x = 1; --x;", "cannot assign value to final variable");
			assertFailCompile("for(final " + t + " i = 0; i < 10; i++){}", "modifiers not allowed");

			// interface field is final on default
			assertFailCompile("interface I{" + t + " x = 1;} I.x = 2;", "cannot assign value to final variable");
			assertFailCompile("interface I{static " + t + " x = 1;} I.x = 2;", "cannot assign value to final variable");
		}

		// var
		assertFailCompile("final var x = 1; x = 2;", "cannot assign value to final variable");

		// boolean operations which changes value
		assertFailCompile("final boolean x = false; x &= true;", "cannot assign value to final variable");
		assertFailCompile("final boolean x = false; x &&= true;", "invalid expression");
		assertFailCompile("final boolean x = false; x |= true;", "cannot assign value to final variable");
		assertFailCompile("final boolean x = false; x ||= true;", "invalid expression");
		assertFailCompile("final boolean x = false; x ^= true;", "cannot assign value to final variable");
		assertFailCompile("final boolean x = false; x ~= true;");
	}

	@Test
	public void testMethod() {
		assertSuccessSerialize("class A{final void get(){}} new A().get();");
		assertFailCompile("class A{final void get(){}} class B extends A{void get(){}}", "cannot rewrite final method");
	}

	@Test
	public void testClass() {
		assertSuccessSerialize("final class A{} new A();");
		assertFailCompile("final class A{} class B extends A{}", "the type B cannot subclass the final class A");
	}
}