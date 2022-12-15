package ru.nest.hiscript.tokenizer;

import ru.nest.hiscript.ool.model.Codeable;
import ru.nest.hiscript.ool.model.nodes.CodeContext;
import ru.nest.hiscript.ool.model.nodes.DecodeContext;

import java.io.IOException;

public class Token implements Codeable {
	public Token(int line, int offset, int length, int lineOffset) {
		this.offset = offset;
		this.length = length;
		this.line = line;
		this.lineOffset = lineOffset;
	}

	public Token(Token start, Token end) {
		this.offset = start.offset;
		this.length = end.offset + end.length - start.offset;
		this.line = start.line;
		this.lineOffset = start.lineOffset;
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
}
