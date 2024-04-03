package ru.nest.hiscript.tokenizer;

public class CharToken extends Token {
	public CharToken(char c, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.c = c;
	}

	private final char c;

	public char getChar() {
		return c;
	}

	@Override
	public String toString() {
		return "Char [" + c + ", " + super.toString() + "]";
	}
}
