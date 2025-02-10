package ru.nest.hiscript.tokenizer;

public class CharTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		if (tokenizer.getCurrent() == '\'') {
			int offset = tokenizer.getOffset();
			int line = tokenizer.getLine();
			int lineOffset = tokenizer.getLineOffset();
			tokenizer.next();

			char c;
			if (tokenizer.getCurrent() == '\'') {
				tokenizer.error("empty character literal", line, offset, 2, lineOffset);
				c = (char) 0;
			} else {
				c = readCharacter(tokenizer);
				int length = 1;
				while (tokenizer.getCurrent() != '\'' && tokenizer.hasNext()) {
					length++;
					if (tokenizer.getCurrent() == ';' || tokenizer.getCurrent() == '\n') {
						break;
					}
					readCharacter(tokenizer);
				}
				if (tokenizer.getCurrent() != '\'') {
					tokenizer.error("' is expected", line, offset + 2, 1, lineOffset);
				} else if (length > 1) {
					tokenizer.error("too many characters in character literal", line, offset, length + 2, lineOffset);
				}
			}
			tokenizer.next();
			return new CharToken(c, line, offset, tokenizer.getOffset() + 1 - offset, lineOffset);
		}
		return null;
	}

	public static char readCharacter(Tokenizer tokenizer) throws TokenizerException {
		int line = tokenizer.getLine();
		int offset = tokenizer.getOffset();
		int lineOffset = tokenizer.getLineOffset();
		char c = tokenizer.getCurrent();
		if (c == '\\') {
			tokenizer.next();
			switch (tokenizer.getCurrent()) {
				case 'n':
					c = '\n';
					break;
				case 't':
					c = '\t';
					break;
				case 'r':
					c = '\r';
					break;
				case 'b':
					c = '\b';
					break;
				case 'f':
					c = '\f';
					break;
				case '\"':
					c = '\"';
					break;
				case '\'':
					c = '\'';
					break;
				case '\\':
					c = '\\';
					break;
				case 'u':
					int size = 0;
					int value = 0;
					while (tokenizer.hasNext()) {
						char next = tokenizer.lookForward();
						if (next >= '0' && next <= '9') {
							value = 16 * value + next - '0';
							size++;
						} else if (next >= 'a' && next <= 'f') {
							value = 16 * value + 10 + next - 'a';
							size++;
						} else if (next >= 'A' && next <= 'F') {
							value = 16 * value + 10 + next - 'A';
							size++;
						} else {
							break;
						}

						tokenizer.next();
						if (size == 4) {
							break;
						}
					}

					if (size == 4 && Character.isValidCodePoint(value)) {
						c = (char) value;
					} else {
						tokenizer.error("invalid code of character", line, offset + 1, size, lineOffset + 1);
					}
					break;
				default:
					c = tokenizer.getCurrent();
					if (c >= '0' && c <= '7') {
						value = c - '0';

						char next = tokenizer.lookForward();
						if (next >= '0' && next <= '7') {
							tokenizer.next();
							value = 8 * value + next - '0';

							next = tokenizer.lookForward();
							if (next >= '0' && next <= '7') {
								int nextValue = 8 * value + next - '0';
								if (nextValue < 256) {
									tokenizer.next();
									value = nextValue;
								}
							}
						}
						c = (char) value;
					} else {
						tokenizer.error("illegal escape character", tokenizer.getLine(), tokenizer.getOffset(), 1, tokenizer.getLineOffset());
					}
			}
		}

		tokenizer.next();
		return c;
	}
}
