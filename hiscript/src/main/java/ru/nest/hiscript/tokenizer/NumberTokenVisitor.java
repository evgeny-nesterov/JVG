package ru.nest.hiscript.tokenizer;

public class NumberTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		boolean negative = false;
		boolean hasSign = false;
		if (tokenizer.hasNext() && (tokenizer.getCurrent() == '-' || tokenizer.getCurrent() == '+')) {
			hasSign = true;
			tokenizer.start();
			negative = tokenizer.getCurrent() == '-';
			tokenizer.next();
			while (tokenizer.hasNext()) {
				if (tokenizer.getCurrent() == '-') {
					tokenizer.next();
					negative = !negative;
				} else if (tokenizer.getCurrent() == '+') {
					tokenizer.next();
				} else {
					break;
				}
			}
		}

		int offset = tokenizer.getOffset();
		boolean hasIntegerPart = false;

		ZERO:
		if (tokenizer.getCurrent() == '0') {
			long value = 0;
			boolean tooLarge = false;
			boolean firstUnderscore = false;
			boolean lastUnderscore = false;
			hasIntegerPart = true;

			// hex (0x...)
			if (tokenizer.lookForward() == 'x') {
				tokenizer.next();
				tokenizer.next();
				char c = tokenizer.getCurrent();
				if (c == '_') {
					firstUnderscore = true;
				}
				if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F') || c == '_') {
					do {
						c = tokenizer.getCurrent();
						long newValue;
						if (c >= '0' && c <= '9') {
							lastUnderscore = false;
							newValue = 16 * value + c - '0';
						} else if (c >= 'a' && c <= 'f') {
							lastUnderscore = false;
							newValue = 16 * value + 10 + c - 'a';
						} else if (c >= 'A' && c <= 'F') {
							lastUnderscore = false;
							newValue = 16 * value + 10 + c - 'A';
						} else if (c == '_') {
							newValue = value;
							lastUnderscore = true;
						} else {
							break;
						}
						if (newValue < value) {
							tooLarge = true;
						}
						value = newValue;
						tokenizer.next();
					} while (tokenizer.hasNext());
				} else {
					tokenizer.error("hexadecimal numbers must contain at least one hexadecimal digit", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
			}

			// binary (0b...)
			else if (tokenizer.lookForward() == 'b') {
				tokenizer.next();
				tokenizer.next();
				char c = tokenizer.getCurrent();
				if (c == '_') {
					firstUnderscore = true;
				}
				if (c == '0' || c == '1' || c == '_') {
					do {
						c = tokenizer.getCurrent();
						long newValue = 0;
						if (c == '0') {
							lastUnderscore = false;
							newValue = 2 * value;
						} else if (c == '1') {
							lastUnderscore = false;
							newValue = 2 * value + 1;
						} else if (c == '_') {
							newValue = value;
							lastUnderscore = true;
						} else {
							break;
						}
						if (newValue < value) {
							tooLarge = true;
						}
						value = newValue;
						tokenizer.next();
					} while (tokenizer.hasNext());
				} else {
					tokenizer.error("binary numbers must contain at least one binary digit", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
			}

			// octal (0...)
			else {
				tokenizer.next();
				do {
					char c = tokenizer.getCurrent();
					long newValue;
					if (c >= '0' && c <= '7') {
						lastUnderscore = false;
						newValue = 8 * value + c - '0';
					} else if (c == '8' || c == '9' || c == '.' || c == 'e' || c == 'E' || c == 'f' || c == 'F' || c == 'd' || c == 'D') {
						// non octal
						break ZERO;
					} else if (c == '_') {
						newValue = value;
						lastUnderscore = true;
					} else {
						break;
					}
					if (newValue < value) {
						tooLarge = true;
					}
					value = newValue;
					tokenizer.next();
				} while (tokenizer.hasNext());
			}

			if (negative) {
				value = -value;
			}

			if (firstUnderscore || lastUnderscore) {
				tokenizer.error("illegal underscore", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}

			if (tokenizer.getCurrent() == 'l' || tokenizer.getCurrent() == 'L') {
				if (tooLarge && value != Long.MIN_VALUE) {
					tokenizer.error("long number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
				tokenizer.next();
				return new LongToken(value, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			} else {
				if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
					tokenizer.error("integer number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
				return new IntToken((int) value, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			}
		}

		hasIntegerPart |= visitInteger(tokenizer, line, offset, lineOffset);

		boolean hasPoint = false;
		boolean hasFloatPart = false;
		if (tokenizer.hasNext() && tokenizer.getCurrent() == '.') {
			hasPoint = true;
			tokenizer.next();

			hasFloatPart = visitInteger(tokenizer, line, offset, lineOffset);
		}

		if ((!hasIntegerPart && !hasPoint) || (hasPoint && !hasIntegerPart && !hasFloatPart)) {
			if (hasSign) {
				tokenizer.rollback();
			}
			return null;
		}

		boolean hasExponent = false;
		boolean hasMantissa = false;
		if (tokenizer.hasNext() && (tokenizer.getCurrent() == 'e' || tokenizer.getCurrent() == 'E')) {
			hasExponent = true;
			tokenizer.next();

			if (tokenizer.hasNext()) {
				if (tokenizer.getCurrent() == '-') {
					tokenizer.next();
				} else if (tokenizer.getCurrent() == '+') {
					tokenizer.next();
				}
			}

			hasMantissa = visitInteger(tokenizer, line, offset, lineOffset);
		}

		if (hasExponent && !hasMantissa) {
			if (hasSign) {
				tokenizer.rollback();
			}
			return null;
		}

		boolean isFloat = false;
		boolean isLong = false;
		boolean isDouble = false;
		if (tokenizer.hasNext() && (tokenizer.getCurrent() == 'f' || tokenizer.getCurrent() == 'F')) {
			isFloat = true;
			tokenizer.next();
		} else if (!hasPoint && !hasExponent && tokenizer.hasNext() && (tokenizer.getCurrent() == 'l' || tokenizer.getCurrent() == 'L')) {
			isLong = true;
			tokenizer.next();
		} else if (tokenizer.hasNext() && (tokenizer.getCurrent() == 'd' || tokenizer.getCurrent() == 'D')) {
			isDouble = true;
			tokenizer.next();
		}

		String text = tokenizer.getText(offset, tokenizer.getOffset() - (isLong || isFloat || isDouble ? 1 : 0), '_');
		if (isLong) {
			long number;
			try {
				number = Long.parseLong(text);
			} catch (NumberFormatException exc) {
				tokenizer.error("long number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				number = 0;
			}
			if (negative) {
				number = -number;
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new LongToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		} else if (isFloat) {
			float number;
			try {
				number = Float.parseFloat(text);
				if (Float.isInfinite(number)) {
					tokenizer.error("float number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				} else if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				throw new RuntimeException("bad number format: " + text);
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new FloatToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		} else if (hasPoint || hasExponent || isDouble) {
			double number;
			try {
				number = Double.parseDouble(text);
				if (Double.isInfinite(number)) {
					tokenizer.error("double number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				} else if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				throw new RuntimeException("bad number format: " + text);
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new DoubleToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		} else {
			long number;
			boolean tooLarge = false;
			try {
				number = Long.parseLong(text);
				if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				tooLarge = true;
				number = 0;
			}
			if (tooLarge || number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
				tokenizer.error("integer number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new IntToken((int) number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		}
	}

	private boolean visitInteger(Tokenizer tokenizer, int line, int offset, int lineOffset) throws TokenizerException {
		// decimal (nmmm, n not 0, m=[0-9]) or octal (0nmmm, n not null, m = [0-7])
		boolean found = false;
		if (tokenizer.hasNext() && tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') {
			found = true;
			tokenizer.next();

			boolean lastUnderscore = false;
			while (tokenizer.hasNext() && ((tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') || tokenizer.getCurrent() == '_')) {
				lastUnderscore = tokenizer.getCurrent() == '_';
				tokenizer.next();
			}

			if (lastUnderscore) {
				tokenizer.error("illegal underscore", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}
		}
		return found;
	}
}
