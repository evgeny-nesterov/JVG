package ru.nest.hiscript.tokenizer;

public class IntToken extends NumberToken {
	public IntToken(int number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private final int number;

	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Int [" + number + ", " + super.toString() + "]";
	}
}
