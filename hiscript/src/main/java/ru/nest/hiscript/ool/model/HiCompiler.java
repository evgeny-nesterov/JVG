package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compiler.ParseRule;
import ru.nest.hiscript.ool.compiler.RootParseRule;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.io.InputStream;

public class HiCompiler {
	private Tokenizer tokenizer;

	private boolean assertsActive = false;

	private ParseRule<?> rule;

	public HiCompiler(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		this.rule = new RootParseRule(this, true);
	}

	public Node build() throws TokenizerException, ParseException {
		Node node = rule.visit(tokenizer, null);
		return node;
	}

	public void setRule(ParseRule<?> rule) {
		this.rule = rule;
	}

	public static HiCompiler getDefaultCompiler(String s) {
		Tokenizer t = Tokenizer.getDefaultTokenizer(s);
		return new HiCompiler(t);
	}

	public static HiCompiler getDefaultCompiler(InputStream is) throws IOException {
		Tokenizer t = Tokenizer.getDefaultTokenizer(is);
		return new HiCompiler(t);
	}

	public boolean isAssertsActive() {
		return assertsActive;
	}

	public void setAssertsActive(boolean assertsActive) {
		this.assertsActive = assertsActive;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}
}
