package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compiler.ParseRule;
import ru.nest.hiscript.ool.compiler.RootParseRule;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.InputStream;

public class Compiler {
	private Tokenizer tokenizer;

	private boolean assertsActive = false;

	public Compiler(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
	}

	public Node build() throws TokenizerException, ParseException {
		Node node = rule.visit(tokenizer, null);
		return node;
	}

	private ParseRule<?> rule = RootParseRule.getInstanceWrapped();

	public void setRule(ParseRule<?> rule) {
		this.rule = rule;
	}

	public static Compiler getDefaultCompiler(String s) {
		Tokenizer t = Tokenizer.getDefaultTokenizer(s);
		return new Compiler(t);
	}

	public static void main(String[] args) {
		testExecutor();
	}

	public static void testExecutor() {
		StringBuilder buf = new StringBuilder();
		try {
			InputStream is = Compiler.class.getResourceAsStream("/oolTestSingle.hi");
			int c;
			while ((c = is.read()) != -1) {
				buf.append((char) c);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Compiler compiler = getDefaultCompiler(buf.toString());
		compiler.setAssertsActive(true);
		try {
			Node node = compiler.build();
			if (node != null) {
//				CodeContext ctxCode = new CodeContext();
//				node.code(ctxCode);
//
//				byte[] res = ctxCode.code();
//
//				System.out.println("======================");
//				ctxCode.statistics();
//				System.out.println("total: " + res.length + " bytes");
//				System.out.println("======================");
//
//				System.out.println("\n" + new String(res));
//				System.out.println("======================");
//
//				DecodeContext ctxDecode = new DecodeContext(res);
//				node = ctxDecode.load();

				// execute
				RuntimeContext ctx = new RuntimeContext(true);
				node.execute(ctx);
				ctx.throwExceptionIf(true);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}

	public boolean isAssertsActive() {
		return assertsActive;
	}

	public void setAssertsActive(boolean assertsActive) {
		this.assertsActive = assertsActive;
	}
}
