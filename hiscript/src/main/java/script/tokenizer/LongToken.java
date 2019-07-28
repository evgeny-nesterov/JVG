package script.tokenizer;

public class LongToken extends NumberToken {
	public LongToken(long number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private long number;

	public long getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Long [" + number + ", " + super.toString() + "]";
	}
}
