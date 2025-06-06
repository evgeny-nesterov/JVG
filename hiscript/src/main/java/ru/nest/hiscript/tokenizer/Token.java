package ru.nest.hiscript.tokenizer;

import ru.nest.hiscript.ool.model.Codeable;
import ru.nest.hiscript.ool.model.TokenAccessible;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class Token implements Codeable, TokenAccessible {
	public Token(int line, int offset, int length, int lineOffset) {
		this.offset = offset;
		this.length = length;
		this.line = line;
		this.lineOffset = lineOffset;
	}

	public Token(Token token) {
		this(token.line, token.offset, token.length, token.lineOffset);
	}

	public Token(Token start, Token end) {
		this(start);
		extend(end);
	}

	public void extend(Token token) {
		if (token == null) {
			return;
		}
		offset = Math.min(offset, token.offset);
		length = Math.max(offset + length, token.offset + token.length) - offset;
		if (offset > token.getOffset()) {
			line = token.line;
			lineOffset = token.lineOffset;
		}
	}

	public Token bounds() {
		return getClass() == Token.class ? this : new Token(line, offset, length, lineOffset);
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
		return (line + 1) + " : " + (lineOffset + 1) + " / " + offset + " - " + (offset + length);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		os.writeInt(line);
		if (line != -1) {
			os.writeInt(offset);
			os.writeInt(length);
			os.writeInt(lineOffset);
		}
	}

	public static Token decode(DecodeContext os) throws IOException {
		int line = os.readInt();
		return line != -1 ? new Token(line, os.readInt(), os.readInt(), os.readInt()) : null;
	}

	@Override
	public Token getToken() {
		return this;
	}

	public void setToken(Token token) {
		this.offset = token.offset;
		this.length = token.length;
		this.line = token.line;
		this.lineOffset = token.lineOffset;
	}

	public Token getInnerToken(Tokenizer tokenizer, String text) {
		int innerOffset = tokenizer.indexOf(this, text);
		if (innerOffset != -1) {
			int line = this.line + tokenizer.getLinesCount(this.offset, innerOffset) - 1;
			return new Token(line, innerOffset, text.length(), tokenizer.getLineOffset(innerOffset));
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Token && line == ((Token) o).length && offset == ((Token) o).offset && length == ((Token) o).length && lineOffset == ((Token) o).lineOffset;
	}
}
