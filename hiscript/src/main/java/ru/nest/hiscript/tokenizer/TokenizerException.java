package ru.nest.hiscript.tokenizer;

public class TokenizerException extends Exception {
	public TokenizerException(String msg, int line, int offset, int length, int lineOffset) {
		super(msg);
		this.line = line;
		this.offset = offset;
		this.length = length;
		this.lineOffset = lineOffset;
	}

	public TokenizerException(String msg, Token token) {
		super(msg);
		this.line = token.getLine();
		this.offset = token.getOffset();
		this.length = token.getLength();
		this.lineOffset = token.getLineOffset();
	}

	private int line;

	public int getLine() {
		return line;
	}

	private int offset;

	public int getOffset() {
		return offset;
	}

	private int length;

	public int getLength() {
		return length;
	}

	private int lineOffset;

	public int getLineOffset() {
		return lineOffset;
	}
}
