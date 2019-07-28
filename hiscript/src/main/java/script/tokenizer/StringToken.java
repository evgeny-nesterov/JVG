package script.tokenizer;

public class StringToken extends Token {
	public StringToken(String string, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.string = string;
	}

	private String string;

	public String getString() {
		return string;
	}

	@Override
	public String toString() {
		return "String [" + string + ", " + super.toString() + "]";
	}
}
