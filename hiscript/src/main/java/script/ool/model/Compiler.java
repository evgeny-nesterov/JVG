package script.ool.model;

import java.io.InputStream;

import script.ParseException;
import script.ool.compiler.ParseRule;
import script.ool.compiler.RootParseRule;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class Compiler {
	private Tokenizer tokenizer;

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
			InputStream is = Compiler.class.getResourceAsStream("testScript.txt");
			int c;
			while ((c = is.read()) != -1) {
				buf.append((char) c);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}

		Compiler p = getDefaultCompiler(buf.toString());
		try {
			Node node = p.build();
			if (node != null) {
				// CodeContext ctxCode = new CodeContext();
				// node.code(ctxCode);
				//
				// byte[] res = ctxCode.code();
				//
				// System.out.println("======================");
				// ctxCode.statistics();
				// System.out.println("total: " + res.length + " bytes");
				// System.out.println("======================");

				// System.out.println("\n" + new String(res));
				// System.out.println("======================");

				// DecodeContext ctxDecode = new DecodeContext(res);
				// node = ctxDecode.load();

				// execute
				RuntimeContext ctx = new RuntimeContext(true);
				node.execute(ctx);
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
