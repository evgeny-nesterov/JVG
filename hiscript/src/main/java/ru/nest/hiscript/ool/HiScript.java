package ru.nest.hiscript.ool;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.HiScriptValidationException;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;

public class HiScript implements AutoCloseable {
	private HiClassLoader classLoader;

	private HiCompiler compiler;

	private HiNode node;

	private RootParseRule parseRule;

	private RuntimeContext ctx;

	private long startTime;

	private HiScript() {
		startTime = System.currentTimeMillis();
	}

	public long duration() {
		return System.currentTimeMillis() - startTime;
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

	public HiScript serialize() {
		//		CodeContext ctxCode = new CodeContext();
		//		node.code(ctxCode);
		//
		//		byte[] bytes = ctxCode.code();
		//
		//		// DEBUG
		//		//		System.out.println("======================");
		//		//		ctxCode.statistics();
		//		//		System.out.println("total: " + bytes.length + " bytes");
		//		//		System.out.println("======================");
		//		//
		//		//		System.out.println("\n" + new String(bytes));
		//		//		System.out.println("======================");
		//
		//		HiClassLoader classLoader = new HiClassLoader("test-decoded");
		//		DecodeContext ctxDecode = new DecodeContext(classLoader, bytes);
		//		node = ctxDecode.load();
		return this;
	}

	public static HiScript create() {
		return new HiScript().open();
	}

	public HiScript open() {
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
}