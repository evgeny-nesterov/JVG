package ru.nest.hiscript.ool;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class HiScript implements AutoCloseable {
	private HiClassLoader classLoader;

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
		classLoader = new HiClassLoader("test");
		return this;
	}

	public HiScript compile(String script) throws TokenizerException, HiScriptParseException, HiScriptValidationException {
		compiler = HiCompiler.getDefaultCompiler(classLoader, script);
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
			HiClassLoader classLoader = new HiClassLoader("main");
			DecodeContext ctxDecode = new DecodeContext(classLoader, serializedCode);
			node = ctxDecode.load();
		}
		return this;
	}

	public byte[] getSerializedCode() {
		return serializedCode;
	}

	@Override
	public void close() {
		ctx.close();
		node = null;
		parseRule = null;
		ctx = null;
		HiClass.systemClassLoader.removeClassLoader(compiler.getClassLoader());
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
}