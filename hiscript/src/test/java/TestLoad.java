import org.junit.jupiter.api.Test;
import ru.nest.hiscript.ool.HiScript;

public class TestLoad extends HiTest {
	@Test
	public void testCalcLoad() throws Exception {
		HiScript script = HiScript.create().compile("int x = 0;").execute().printError().compile("x = x + 1;");
		for (int i = 0; i < 1_000_000; i++) {
			script.execute().printError();
		}
		script.compile("System.println(\"x=\" + x); assert x == 1_000_000;").execute().printError().close();
		System.out.println("load test duration: " + script.duration() / 1000.0 + "sec");
	}

	@Test
	public void testStringLoad() throws Exception {
		HiScript script = HiScript.create().compile("String s = \"\";").execute().printError().compile("s = s + \"*\";");
		for (int i = 0; i < 50_000; i++) {
			script.execute().printError();
		}
		script.compile("System.println(\"s.length=\" + s.length()); assert s.length() == 50_000;").execute().printError().close();
		System.out.println("load test duration: " + script.duration() / 1000.0 + "sec");
	}
}