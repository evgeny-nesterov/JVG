import org.junit.jupiter.api.Test;

public class TestTokenizer extends HiTest {
	@Test
	public void testTokenizer() {
		assertFailCompile("ljoi(*&^%^i}_", //
				"unexpected token");
	}
}