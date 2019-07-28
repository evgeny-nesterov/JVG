package script.tokenizer;

public class IntToken extends NumberToken {
	public IntToken(int number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private int number;

	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return "Int [" + number + ", " + super.toString() + "]";
	}
}
