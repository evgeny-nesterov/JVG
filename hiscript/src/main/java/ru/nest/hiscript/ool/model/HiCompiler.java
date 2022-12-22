package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.compile.RootParseRule;
import ru.nest.hiscript.ool.model.validation.ValidationException;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import java.io.IOException;
import java.io.InputStream;

public class HiCompiler {
	private Tokenizer tokenizer;

	private boolean assertsActive = false;

	private boolean verbose = false;

	private boolean printInvalidCode = false;

	private ParseRule<?> rule;

	private ValidationInfo validationInfo;

	private HiClassLoader classLoader;

	public HiCompiler(HiClassLoader classLoader, Tokenizer tokenizer) {
		this.classLoader = classLoader;
		this.tokenizer = tokenizer;
		this.rule = new RootParseRule(this, true);
	}

	public HiNode build() throws TokenizerException, ParseException, ValidationException {
		HiNode node = rule.visit(tokenizer, null);
		validationInfo = new ValidationInfo(this);
		boolean valid = node.validate(validationInfo, null);
		valid &= classLoader.validate(validationInfo);
		if (validationInfo.messages.size() > 0) {
			validationInfo.throwExceptionIf();
		} else if (!valid) {
			throw new ValidationException("Validation error", null);
		}
		return node;
	}

	public ValidationInfo getValidationInfo() {
		return validationInfo;
	}

	public void setRule(ParseRule<?> rule) {
		this.rule = rule;
	}

	public static HiCompiler getDefaultCompiler(HiClassLoader classLoader, String s) {
		Tokenizer t = Tokenizer.getDefaultTokenizer(s);
		return new HiCompiler(classLoader, t);
	}

	public static HiCompiler getDefaultCompiler(InputStream is) throws IOException {
		Tokenizer t = Tokenizer.getDefaultTokenizer(is);
		return new HiCompiler(null, t);
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

	public HiClassLoader getClassLoader() {
		return classLoader;
	}

	public String getTokenText(Token token) {
		return tokenizer.getText(token);
	}

	public String getTokenLineText(Token token) {
		return tokenizer.getTokenLineText(token);
	}

	public boolean isPrintInvalidCode() {
		return printInvalidCode;
	}

	public void setPrintInvalidCode(boolean printInvalidCode) {
		this.printInvalidCode = printInvalidCode;
	}
}
