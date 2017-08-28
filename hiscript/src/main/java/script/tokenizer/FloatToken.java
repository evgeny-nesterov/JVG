package script.tokenizer;

public class FloatToken extends NumberToken {
	public FloatToken(float number, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.number = number;
	}

	private float number;

	public float getNumber() {
		return number;
	}

	public String toString() {
		return "Float [" + number + ", " + super.toString() + "]";
	}
}
