import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.HiScript;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HiTest {
	public void assertCondition(String script, String condition, String message) {
		try {
			execute(script + "\nassert " + condition + " : \"" + message + "\";");
			// expected
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script, String message) {
		try {
			execute(script);
			// expected
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script) {
		try {
			execute(script);
			// expected
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, e.toString());
		}
	}

	public void assertSuccessSerialize(String script) {
		try {
			executeSerialized(script);
			// expected
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, e.toString());
		}
	}

	public void assertSuccessCompile(String script) {
		try {
			compile(script);
			// expected
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, e.toString());
		}
	}

	public void assertFail(String script, String message) {
		try {
			execute(script);
			onFail(script, message);
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			onFail(script, "Compilation failed, expected exception");
		} catch (Exception e) {
			// expected
			System.out.println("Success: expected failure: " + e.getMessage());
		}
	}

	public void assertFail(String script) {
		try {
			execute(script);
			onFail(script, "fail");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			onFail(script, "Compilation failed, expected exception");
		} catch (Exception e) {
			// expected
			System.out.println("Success: expected failure: " + e.getMessage());
		}
	}

	public void assertFailSerialize(String script) {
		try {
			executeSerialized(script);
			onFail(script, "fail");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			onFail(script, "Compilation failed, expected exception");
		} catch (Exception e) {
			// expected
			System.out.println("Success: expected failure: " + e.getMessage());
		}
	}

	public void assertFailCompile(String script) {
		try {
			compile(script);
			onFail(script, "fail (actual success)");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			// expected
			System.out.println("Success: expected compile failure: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, "Expected compile failure");
		}
	}

	public HiScript execute(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		HiScript result = HiScript.create().compile(script).execute().throwExceptionIf();
		result.close();
		return result;
	}

	public HiScript executeSerialized(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		HiScript result = HiScript.create().compile(script).serialize().execute().throwExceptionIf();
		result.close();
		return result;
	}

	public HiScript compile(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		return HiScript.create().compile(script).throwExceptionIf();
	}

	private void onFail(String script, String message) {
		System.out.println("==================== FAIL ======================");
		System.out.println(script);
		System.out.println("================================================");
		fail(message);
	}
}
