package script.tokenizer;

public class ByteToken extends NumberToken {
	public ByteToken(byte number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private byte number;

	public byte getNumber() {
		return number;
	}

	public String toString() {
		return "Byte [" + number + ", " + super.toString() + "]";
	}
}
