package script.tokenizer;

public class DoubleToken extends NumberToken {
	public DoubleToken(double number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private double number;

	public double getNumber() {
		return number;
	}

	public String toString() {
		return "Double [" + number + ", " + super.toString() + "]";
	}
}
