package script.tokenizer;

public abstract class NumberToken extends Token {
	public NumberToken(int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
	}
}
