package ru.nest.hiscript.tokenizer;

import java.util.ArrayList;
import java.util.List;

public class StringTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int line = tokenizer.getLine();
		int offset = tokenizer.getOffset();
		int lineOffset = tokenizer.getLineOffset();
		if (tokenizer.getCurrent() == '"' && tokenizer.hasNext()) {
			tokenizer.next();

			if (tokenizer.getCurrent() == '"') {
				if (tokenizer.hasNext()) {
					tokenizer.next();
					if (tokenizer.getCurrent() == '"') {
						tokenizer.next();
						return getTextBlock(tokenizer, line, offset, lineOffset);
					}
				}

				// empty string
				return new StringToken("", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}

			StringBuilder buf = new StringBuilder();
			boolean closed = false;
			while (tokenizer.hasNext()) {
				if (tokenizer.getCurrent() == '"') {
					tokenizer.next();
					closed = true;
					break;
				}

				char c = CharTokenVisitor.readCharacter(tokenizer);
				buf.append(c);
			}

			StringToken stringToken = new StringToken(buf.toString(), line, offset, tokenizer.getOffset() - offset, lineOffset);
			if (!closed) {
				tokenizer.error("'\"' is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
			}
			return stringToken;
		}
		return null;
	}

	private Token getTextBlock(Tokenizer tokenizer, int line, int offset, int lineOffset) throws TokenizerException {
		tokenizer.skipLineWhitespaces();
		if (tokenizer.getCurrent() != '\n') {
			tokenizer.error("new line is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
		}
		tokenizer.next();

		StringBuilder lastLine = new StringBuilder();
		List<StringBuilder> lines = new ArrayList<>();
		int quotesCount = 0;
		boolean startLine = true;
		int minStartLineWhiteSpaces = Integer.MAX_VALUE;
		int startLineWhiteSpaces = 0;
		while (tokenizer.hasNext()) {
			char c = CharTokenVisitor.readCharacter(tokenizer);
			if (c == '\n') {
				startLineWhiteSpaces = 0;
				startLine = true;

				// remove whitespaces at the end of line
				while (lastLine.length() > 0 && Tokenizer.isWhiteSpace(lastLine.charAt(lastLine.length() - 1))) {
					lastLine.setLength(lastLine.length() - 1);
				}
				lines.add(lastLine);
				lastLine = new StringBuilder();
				continue;
			} else if (startLine && (c == ' ' || c == '\t')) {
				startLineWhiteSpaces++;
			} else {
				startLine = false;
				if (startLineWhiteSpaces < minStartLineWhiteSpaces) {
					minStartLineWhiteSpaces = startLineWhiteSpaces;
				}
				if (c == '"') {
					quotesCount++;
					if (quotesCount == 3) {
						break;
					}
				} else {
					quotesCount = 0;
				}
			}
			lastLine.append(c);
		}

		if (quotesCount != 3) {
			tokenizer.error("'\"\"\"' is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
		}

		lastLine.setLength(lastLine.length() - 2); // delete last two quotes
		if (startLineWhiteSpaces < minStartLineWhiteSpaces) {
			minStartLineWhiteSpaces = startLineWhiteSpaces;
		}
		while (lastLine.length() > startLineWhiteSpaces && Tokenizer.isWhiteSpace(lastLine.charAt(lastLine.length() - 1))) {
			lastLine.setLength(lastLine.length() - 1);
		}
		lines.add(lastLine);

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < lines.size(); i++) {
			StringBuilder lineBuf = lines.get(i);
			if (i > 0) {
				buf.append('\n');
			}
			buf.append(lineBuf, minStartLineWhiteSpaces, lineBuf.length());
		}
		return new StringToken(buf.toString(), line, offset, tokenizer.getOffset() - offset, lineOffset);
	}
}
