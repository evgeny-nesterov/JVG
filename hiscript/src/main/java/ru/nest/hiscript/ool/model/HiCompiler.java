package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compiler.ParseRule;
import ru.nest.hiscript.ool.compiler.RootParseRule;
import ru.nest.hiscript.ool.model.validation.ValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.io.InputStream;

public class HiCompiler {
	private Tokenizer tokenizer;

	private boolean assertsActive = false;

	private boolean verbose = false;

	private ParseRule<?> rule;

	public HiCompiler(Tokenizer tokenizer) {
		this.tokenizer = tokenizer;
		this.rule = new RootParseRule(this, true);
	}

	public Node build() throws TokenizerException, ParseException, ValidationException {
		Node node = rule.visit(tokenizer, null);
		ValidationInfo validationInfo = new ValidationInfo(this);
		if (!node.validate(validationInfo, null)) {
			validationInfo.throwExceptionIf();
		}
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

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public Tokenizer getTokenizer() {
		return tokenizer;
	}
}
