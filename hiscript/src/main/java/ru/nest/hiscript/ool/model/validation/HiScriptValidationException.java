package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.tokenizer.Token;

public class HiScriptValidationException extends Exception {
	public HiScriptValidationException(String msg, Token token) {
		super(msg + ": " + (token != null ? token : "EOF"));
		this.token = token;
	}

	private final Token token;

	public Token getToken() {
		return token;
	}

	private ValidationInfo validationInfo;

	public ValidationInfo getValidationInfo() {
		return validationInfo;
	}

	public HiScriptValidationException setValidationInfo(ValidationInfo validationInfo) {
		this.validationInfo = validationInfo;
		return this;
	}
}
