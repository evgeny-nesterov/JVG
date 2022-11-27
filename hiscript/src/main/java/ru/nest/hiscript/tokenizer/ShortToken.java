package ru.nest.hiscript.tokenizer;

public class ShortToken extends NumberToken {
	public ShortToken(short number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private short number;

	public short getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Short [" + number + ", " + super.toString() + "]";
	}
}
