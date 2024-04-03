package ru.nest.hiscript.tokenizer;

public abstract class NumberToken extends Token {
	private final boolean hasSign;

	public NumberToken(int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset);
		this.hasSign = hasSign;
	}

	public boolean hasSign() {
		return hasSign;
	}
}
