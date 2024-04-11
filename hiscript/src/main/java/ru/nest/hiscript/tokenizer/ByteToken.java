package ru.nest.hiscript.tokenizer;

/**
 * Used IntToken instead
 */
@Deprecated
public class ByteToken extends NumberToken {
	public ByteToken(byte number, int line, int offset, int length, int lineOffset, boolean hasSign) {
		super(line, offset, length, lineOffset, hasSign);
		this.number = number;
	}

	private final byte number;

	public byte getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Byte [" + number + ", " + super.toString() + "]";
	}
}
