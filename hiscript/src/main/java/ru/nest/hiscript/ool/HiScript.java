package ru.nest.hiscript.ool;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.ool.runtime.HiRuntimeEnvironment;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.util.List;

public class HiScript implements AutoCloseable {
	private HiRuntimeEnvironment env;

	private HiCompiler compiler;

	private HiNodeIF node;

	private RootParseRule parseRule;

	private RuntimeContext ctx;

	private final long startTime;

	private byte[] serializedCode;

	public static HiScript create() {
		return new HiScript().open();
	}

	private HiScript() {
		startTime = System.currentTimeMillis();
	}

	public HiScript open() {
		env = new HiRuntimeEnvironment();
		return this;
	}

	public HiRuntimeEnvironment getEnv() {
		return env;
	}

	public HiScript compile(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		compiler = HiCompiler.getDefaultCompiler(env.getUserClassLoader(), script);
		compiler.setAssertsActive(true);
		compiler.setVerbose(true);
		compiler.setPrintInvalidCode(true);
		if (parseRule == null) {
			parseRule = new RootParseRule(compiler, true, true);
		}
		compiler.setRule(parseRule);
		node = compiler.build();
		return this;
	}

	private CodeContext ctxCode;

	public HiScript serialize() throws IOException {
		if (node != null) {
			ctxCode = new CodeContext();
			node.code(ctxCode);
			serializedCode = ctxCode.code();
		}
		return this;
	}

	public HiScript printSerializeDebug() {
		if (ctxCode != null) {
			System.out.println("======================");
			ctxCode.statistics();
			System.out.println("total: " + serializedCode.length + " bytes");
			System.out.println("======================");

			System.out.println("\n" + new String(serializedCode));
			System.out.println("======================");
		}
		return this;
	}

	public HiScript deserialize() throws IOException {
		if (serializedCode != null) {
			clear();
			DecodeContext ctxDecode = new DecodeContext(env.getUserClassLoader(), serializedCode);
			node = ctxDecode.load();
		}
		return this;
	}

	public HiScript clear() throws IOException {
		env.clear();
		return this;
	}

	public byte[] getSerializedCode() {
		return serializedCode;
	}

	public HiScript registerNative(List objects) {
		env.getUserClassLoader().getNative().registerObjects(objects);
		return this;
	}

	public HiScript execute() {
		if (ctx == null) {
			ctx = new RuntimeContext(compiler, true);
		}
		node.execute(ctx);
		return this;
	}

	public boolean hasValidationException() {
		return !compiler.getValidationInfo().isValid();
	}

	public boolean hasRuntimeException() {
		return ctx.exception != null;
	}

	public HiScript throwExceptionIf() throws HiScriptValidationException {
		compiler.getValidationInfo().throwExceptionIf();
		if (ctx != null) {
			ctx.throwExceptionIf(compiler.isVerbose());
		}
		return this;
	}

	public HiScript printError() {
		compiler.getValidationInfo().printError();
		ctx.printException();
		return this;
	}

	public long getDuration() {
		return System.currentTimeMillis() - startTime;
	}

	@Override
	public void close() {
		ctx.close();
		node = null;
		parseRule = null;
		ctx = null;
		env.clear();
	}
}