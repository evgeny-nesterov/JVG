package ru.nest.hiscript;

import ru.nest.hiscript.tokenizer.Token;

public class HiScriptParseException extends Exception {
	public HiScriptParseException(String msg, Token token) {
		super(msg + ": " + (token != null ? token : "EOF"));
		this.token = token;
	}

	private final Token token;

	public Token getToken() {
		return token;
	}
}
