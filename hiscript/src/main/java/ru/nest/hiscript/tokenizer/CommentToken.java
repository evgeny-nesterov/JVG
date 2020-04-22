package ru.nest.hiscript.tokenizer;

public class CommentToken extends Token {
	public CommentToken(int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
	}

	@Override
	public String toString() {
		return "Comment [" + super.toString() + "]";
	}
}
