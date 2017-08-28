package script.tokenizer;

public class ShortToken extends NumberToken {
	public ShortToken(short number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private short number;

	public short getNumber() {
		return number;
	}

	public String toString() {
		return "Short [" + number + ", " + super.toString() + "]";
	}
}
