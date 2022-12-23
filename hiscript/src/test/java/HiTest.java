import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.ValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public abstract class HiTest {
	public void assertCondition(String script, String condition, String message) {
		try {
			execute(script + "\nassert " + condition + " : \"" + message + "\";", false);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script, String message) {
		try {
			execute(script, false);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, message);
		}
	}

	public void assertSuccess(String script) {
		try {
			execute(script, false);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, "fail");
		}
	}

	public void assertSuccessSerialize(String script) {
		try {
			execute(script, true);
		} catch (Exception e) {
			e.printStackTrace();
			onFail(script, "fail");
		}
	}

	public void assertFail(String script, String message) {
		try {
			execute(script, false);
			onFail(script, message);
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	public void assertFail(String script) {
		try {
			execute(script, false);
			onFail(script, "fail");
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	public void assertFailSerialize(String script) {
		try {
			execute(script, true);
			onFail(script, "fail");
		} catch (TokenizerException e) {
			e.printStackTrace();
		} catch (Exception e) {
		}
	}

	class CompiledNode implements AutoCloseable {
		HiClassLoader classLoader;

		HiCompiler compiler;

		HiNode node;

		RootParseRule parseRule;

		RuntimeContext ctx;

		CompiledNode compile(String script, boolean serialize) throws TokenizerException, ParseException, IOException, ValidationException {
			compiler = HiCompiler.getDefaultCompiler(classLoader, script);
			compiler.setAssertsActive(true);
			compiler.setVerbose(true);
			compiler.setPrintInvalidCode(true);
			if (parseRule == null) {
				parseRule = new RootParseRule(compiler, true);
			}
			compiler.setRule(parseRule);
			node = compiler.build();
			if (serialize) {
				// node = serialize(node);
			}
			return this;
		}

		CompiledNode open() {
			classLoader = new HiClassLoader("test");
			return this;
		}

		@Override
		public void close() {
			ctx.close();
			node = null;
			parseRule = null;
			ctx = null;
			HiClass.systemClassLoader.removeClassLoader(compiler.getClassLoader());
		}

		CompiledNode execute(boolean throwException) throws TokenizerException, ParseException, IOException, ValidationException {
			if (ctx == null) {
				ctx = new RuntimeContext(compiler, true);
			}
			node.execute(ctx);
			if (throwException) {
				ctx.throwExceptionIf(true);
			}
			return this;
		}
	}

	public CompiledNode execute(String script) throws TokenizerException, ParseException, IOException, ValidationException {
		return execute(script, false);
	}

	public CompiledNode execute(String script, boolean serialize) throws TokenizerException, ParseException, IOException, ValidationException {
		CompiledNode result = new CompiledNode().open().compile(script, serialize).execute(true);
		result.close();
		return result;
	}

	public CompiledNode compile(String script) throws TokenizerException, ParseException, IOException, ValidationException {
		return new CompiledNode().compile(script, false);
	}

	private void onFail(String script, String message) {
		System.out.println("================================================");
		System.out.println(script);
		System.out.println("================================================");
		fail(message);
	}

	public HiNode serialize(HiNode node) throws IOException {
		CodeContext ctxCode = new CodeContext();
		node.code(ctxCode);

		byte[] bytes = ctxCode.code();

		// DEBUG
		//		System.out.println("======================");
		//		ctxCode.statistics();
		//		System.out.println("total: " + bytes.length + " bytes");
		//		System.out.println("======================");
		//
		//		System.out.println("\n" + new String(bytes));
		//		System.out.println("======================");

		HiClassLoader classLoader = new HiClassLoader("test-decoded");
		DecodeContext ctxDecode = new DecodeContext(classLoader, bytes);
		return ctxDecode.load();
	}
}
