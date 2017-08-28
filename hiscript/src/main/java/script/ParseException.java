package script;

import script.tokenizer.Token;

public class ParseException extends Exception {
	public ParseException(String msg, Token token) {
		super(msg + ": " + (token != null ? token : "EOF"));
		this.token = token;
	}

	private Token token;

	public Token getToken() {
		return token;
	}
}
