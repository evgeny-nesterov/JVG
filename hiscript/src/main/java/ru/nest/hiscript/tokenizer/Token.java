package ru.nest.hiscript.tokenizer;

public class Token {
	public Token(int line, int offset, int length, int lineOffset) {
		this.offset = offset;
		this.length = length;
		this.line = line;
		this.lineOffset = lineOffset;
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

	@Override
	public String toString() {
		return (line + 1) + " : " + (lineOffset + 1);
	}
}
