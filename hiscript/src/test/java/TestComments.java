import org.junit.jupiter.api.Test;

public class TestComments extends HiTest {
	@Test
	public void test() {
		assertSuccessSerialize("// test\nint x = 1");
		assertSuccessSerialize("/* test */\nint x = 1");
		assertSuccessSerialize("/** test */\nint x = 1");
		assertFailCompile("int x = 1; /* test", //
				"end of comment '*/' is expected");
	}
}