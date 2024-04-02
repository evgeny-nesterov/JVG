import org.junit.jupiter.api.Test;

public class TestComments extends HiTest {
	@Test
	public void test() {
		assertSuccess("// test\nint x = 1");
		assertSuccess("/* test */\nint x = 1");
		assertSuccess("/** test */\nint x = 1");
		assertFailCompile("int x = 1; /* test");
	}
}