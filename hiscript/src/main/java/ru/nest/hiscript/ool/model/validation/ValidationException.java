package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.tokenizer.Token;

public class ValidationException extends Exception {
	public ValidationException(String msg, Token token) {
		super(msg + ": " + (token != null ? token : "EOF"));
		this.token = token;
	}

	private Token token;

	public Token getToken() {
		return token;
	}
}
