import org.opentest4j.AssertionFailedError;
import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.HiScript;
import ru.nest.hiscript.ool.compile.HiCompiler;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.runtime.HiScriptRuntimeException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class HiTest {
	private List nativeObjects = new ArrayList<>();

	{
		// set to true to completely cover code
		HiCompiler.compilingSystem = false;
	}

	public void assertCondition(String script, String condition, String message) {
		try {
			execute(script + "\nassert " + condition + " : \"" + message + "\";");
			// expected
		} catch (Throwable e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script, String message) {
		try {
			execute(script);
			// expected
		} catch (Throwable e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script) {
		try {
			execute(script);
			// expected
		} catch (Throwable e) {
			if (e instanceof HiScriptRuntimeException) {
				System.out.println(script);
			}
			e.printStackTrace();
			onFail(script, e.toString());
		}
	}

	public void assertSuccessCompile(String script) {
		try {
			compile(script);
			// expected
		} catch (Throwable e) {
			e.printStackTrace();
			onFail(script, e.toString());
		}
	}

	public void assertFail(String script, String expectMessage) {
		try {
			execute(script);
			onFail(script, "executed successfully, expected failure: " + expectMessage);
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			onFail(script, "Compilation failed: expected exception");
		} catch (AssertionFailedError e) {
			throw e;
		} catch (Throwable e) {
			String realMessage = e.getMessage() != null ? e.getMessage() : "";
			if (realMessage.indexOf(expectMessage) != -1) {
				// expected
				System.err.println("Success! Expected failure: " + realMessage);
			} else {
				e.printStackTrace();
				onFail(script, "Failure! Expected message: " + expectMessage + ". Real message: " + realMessage);
			}
		}
	}

	public <E extends Throwable> void assertFail(String script, Class<E> exceptionClass) {
		try {
			execute(script);
			onFail(script, "fail");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			onFail(script, "Compilation failed: expected exception");
		} catch (Throwable e) {
			// expected
			if (e.getClass() == exceptionClass) {
				System.err.println("Success! Expected failure: " + e);
			} else {
				System.err.println("Failure! Expected exception: " + exceptionClass);
				e.printStackTrace();
			}
		}
	}

	@Deprecated
	public void assertFailCompile(String script) {
		try {
			compile(script);
			onFail(script, "fail (actual success)");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			// expected
			System.err.println("Success! Expected compile failure: " + e.getMessage());

//			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
//			clipboard.setContents(new StringSelection(", //\n\"" + e.getMessage() + "\""), null);
		} catch (Throwable e) {
			e.printStackTrace();
			onFail(script, "Expected compile failure");
		}
	}

	public void assertFailCompile(String script, String... expectMessages) {
		try {
			compile(script);
			onFail(script, "fail (actual success)");
		} catch (TokenizerException | HiScriptParseException | HiScriptValidationException e) {
			boolean match = true;
			for (String expectMessage : expectMessages) {
				if (expectMessage.length() == 0) {
					continue;
				}
				boolean matchMessage = e.getMessage() != null && e.getMessage().indexOf(expectMessage) != -1;
				if (!matchMessage && e instanceof HiScriptValidationException) {
					matchMessage = ((HiScriptValidationException) e).getValidationInfo().hasMessage(expectMessage);
				}
				if (!matchMessage) {
					onFail(script, "Failure! Expected compile message: " + expectMessage + ". Real message: " + e.getMessage());
					match = false;
				}
			}
			if (!match) {
				e.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			onFail(script, "Expected compile failure");
		}
	}

	public HiScript execute(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException, IOException {
		HiScript result = HiScript.create().compile(script).registerNative(nativeObjects). //
				serialize().deserialize(). //
				execute().throwExceptionIf();
		result.close();
		return result;
	}

	public HiScript compile(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		return HiScript.create().compile(script).throwExceptionIf();
	}

	public HiTest nativeObject(Object object) {
		nativeObjects.add(object);
		return this;
	}

	private void onFail(String script, String message) {
		System.err.println("==================== FAIL ======================");
		System.err.println(script);
		System.err.println("================================================");
		fail(message);
	}
}
