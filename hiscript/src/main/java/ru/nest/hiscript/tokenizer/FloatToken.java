package ru.nest.hiscript.tokenizer;

public class FloatToken extends NumberToken {
	public FloatToken(float number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private float number;

	public float getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Float [" + number + ", " + super.toString() + "]";
	}
}
