package ru.nest.hiscript.tokenizer;

public class DoubleToken extends NumberToken {
	public DoubleToken(double number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private double number;

	public double getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Double [" + number + ", " + super.toString() + "]";
	}
}
