import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Compiler;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class HiTest {
	public void assertCondition(String script, String condition, String message) {
		try {
			execute(script + "\nassert " + condition + " : \"" + message + "\";");
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script, String message) {
		try {
			execute(script);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script) {
		try {
			execute(script);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, "fail");
		}
	}

	public void assertFail(String script, String message) {
		try {
			execute(script);
			onFail(script, message);
		} catch (Exception e) {
		}
	}

	public void assertFail(String script) {
		try {
			execute(script);
			onFail(script, "fail");
		} catch (Exception e) {
		}
	}

	public void execute(String script) throws TokenizerException, ParseException {
		Compiler compiler = Compiler.getDefaultCompiler(script);
		compiler.setAssertsActive(true);
		Node node = compiler.build();
		if (node != null) {
			RuntimeContext ctx = new RuntimeContext(true);
			node.execute(ctx);
			ctx.throwExceptionIf(true);
		}
		// problems! HiClass.clearClassLoader();
	}

	private void onFail(String script, String message) {
		System.out.println("================================================");
		System.out.println(script);
		System.out.println("================================================");
		fail(message);
	}
}
