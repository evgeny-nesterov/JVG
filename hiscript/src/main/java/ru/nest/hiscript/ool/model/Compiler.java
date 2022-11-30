package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compiler.ParseRule;
import ru.nest.hiscript.ool.compiler.RootParseRule;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
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

	public static Compiler getDefaultCompiler(InputStream is) throws IOException {
		Tokenizer t = Tokenizer.getDefaultTokenizer(is);
		return new Compiler(t);
	}

	public boolean isAssertsActive() {
		return assertsActive;
	}

	public void setAssertsActive(boolean assertsActive) {
		this.assertsActive = assertsActive;
	}
}
