package ru.nest.hiscript.tokenizer;

public class LongToken extends NumberToken {
	public LongToken(long number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private final long number;

	public long getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Long [" + number + ", " + super.toString() + "]";
	}
}
