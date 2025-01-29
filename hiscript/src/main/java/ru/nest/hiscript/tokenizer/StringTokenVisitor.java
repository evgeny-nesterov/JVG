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
				} else if (tokenizer.getCurrent() == '\n' || tokenizer.getCurrent() == '\r') {
					tokenizer.error("Illegal line end in string literal", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
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
		boolean trimLine = true;
		while (tokenizer.hasNext()) {
			boolean slashedQuote = false;
			if (tokenizer.getCurrent() == '\\') {
				char nextChar = tokenizer.lookForward();
				if (nextChar == 's') {
					tokenizer.next();
					tokenizer.next();
					tokenizer.skipLineWhitespaces();
					trimLine = false;
				} else if (nextChar == '"') {
					tokenizer.next();
					slashedQuote = true;
				}
			}
			char c = CharTokenVisitor.readCharacter(tokenizer);
			if (c == '\n') {
				startLineWhiteSpaces = 0;
				startLine = true;

				// remove whitespaces at the end of line
				if (trimLine) {
					while (lastLine.length() > 0 && Tokenizer.isWhiteSpace(lastLine.charAt(lastLine.length() - 1))) {
						lastLine.setLength(lastLine.length() - 1);
					}
				}
				trimLine = true;

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
				if (c == '"' && !slashedQuote) {
					quotesCount++;
					if (quotesCount == 3) {
						break;
					}
				} else {
					if (!trimLine) {
						tokenizer.error("illegal escape character", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
					}
					quotesCount = 0;
				}
			}
			lastLine.append(c);
		}

		if (quotesCount != 3) {
			tokenizer.error("'\"\"\"' is expected", tokenizer.getLine(), tokenizer.getOffset() - 1, 1, tokenizer.getLineOffset());
		}

		if (lastLine.length() >= 2) {
			lastLine.setLength(lastLine.length() - 2); // delete last two quotes
		}
		if (startLineWhiteSpaces < minStartLineWhiteSpaces) {
			minStartLineWhiteSpaces = startLineWhiteSpaces;
		}
		// remove whitespaces at the end of line (if not ends with \s)
		if (trimLine) {
			while (lastLine.length() > startLineWhiteSpaces && Tokenizer.isWhiteSpace(lastLine.charAt(lastLine.length() - 1))) {
				lastLine.setLength(lastLine.length() - 1);
			}
		}
		// line ends with _
		if (lastLine.length() >= 1 && lastLine.charAt(lastLine.length() - 1) == '_' && lines.size() > 0) {
			lines.get(lines.size() - 1).append(lastLine, 0, lastLine.length() - 1);
		} else {
			lines.add(lastLine);
		}

		StringBuilder buf = new StringBuilder();
		boolean appendPrevLine = false;
		for (int i = 0; i < lines.size(); i++) {
			StringBuilder lineBuf = lines.get(i);

			// line ends with _
			boolean appendLine = false;
			if (lineBuf.length() > 0 && lineBuf.charAt(lineBuf.length() - 1) == '_') {
				lineBuf.setLength(lineBuf.length() - 1);
				appendLine = true;
			}

			if (i > 0 && !appendPrevLine) {
				buf.append('\n');
			}
			buf.append(lineBuf, minStartLineWhiteSpaces, lineBuf.length());
			appendPrevLine = appendLine;
		}
		return new StringToken(buf.toString(), line, offset, tokenizer.getOffset() - offset, lineOffset);
	}
}
