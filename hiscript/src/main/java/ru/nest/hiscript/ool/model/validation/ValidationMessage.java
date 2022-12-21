package ru.nest.hiscript.ool.model.validation;

import ru.nest.hiscript.tokenizer.Token;

public class ValidationMessage {
	public enum ValidationLevel {
		info, warning, error
	}

	public ValidationLevel level;

	public String message;

	public Token token;

	public ValidationMessage(ValidationLevel level, String message, Token token) {
		this.level = level;
		this.message = message;
		this.token = token;
	}
}