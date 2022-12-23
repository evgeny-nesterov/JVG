import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.HiScript;
import ru.nest.hiscript.ool.model.validation.ValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

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

	public void assertSuccessSerialize(String script) {
		try {
			executeSerialized(script);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, "fail");
		}
	}

	public void assertFail(String script, String message) {
		try {
			execute(script);
			onFail(script, message);
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	public void assertFail(String script) {
		try {
			execute(script);
			onFail(script, "fail");
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	public void assertFailSerialize(String script) {
		try {
			executeSerialized(script);
			onFail(script, "fail");
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	public HiScript execute(String script) throws TokenizerException, ParseException, ValidationException {
		HiScript result = new HiScript().open().compile(script).throwException().execute();
		result.close();
		return result;
	}

	public HiScript executeSerialized(String script) throws TokenizerException, ParseException, IOException, ValidationException {
		HiScript result = new HiScript().open().compile(script).serialize().throwException().execute();
		result.close();
		return result;
	}

	public HiScript compile(String script) throws TokenizerException, ParseException, IOException, ValidationException {
		return new HiScript().compile(script);
	}

	private void onFail(String script, String message) {
		System.out.println("================================================");
		System.out.println(script);
		System.out.println("================================================");
		fail(message);
	}
}
