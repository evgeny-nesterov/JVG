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
					negative = !negative;
					tokenizer.next();
				} else if (tokenizer.getCurrent() == '+') {
					tokenizer.next();
				} else {
					break;
				}
			}
		}

		int offset = tokenizer.getOffset();

		// hex
		if (tokenizer.getCurrent() == '0' && tokenizer.lookForward() == 'x') {
			tokenizer.next();
			tokenizer.next();
			char c = tokenizer.getCurrent();
			long value = c;
			boolean tooLarge = false;
			if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')) {
				tokenizer.next();
				while (tokenizer.hasNext()) {
					c = tokenizer.getCurrent();
					long newValue;
					if (c >= '0' && c <= '9') {
						newValue = 16 * value + c - '0';
					} else if (c >= 'a' && c <= 'f') {
						newValue = 16 * value + 10 + c - 'a';
					} else if (c >= 'A' && c <= 'F') {
						newValue = 16 * value + 10 + c - 'A';
					} else {
						break;
					}
					if (newValue < value) {
						tooLarge = true;
					}
					value = newValue;
					tokenizer.nextToken();
				}
			} else {
				tokenizer.error("Invalid number value", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}
			if (negative) {
				value = -value;
			}

			if (tokenizer.lookForward() == 'l' || tokenizer.lookForward() == 'L') {
				if (tooLarge) {
					tokenizer.error("Long number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
				tokenizer.next();
				return new LongToken(value, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			} else {
				if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
					tokenizer.error("Integer number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				}
				return new IntToken((int) value, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			}
		}

		boolean hasIntegerPart = visitInteger(tokenizer);

		boolean hasPoint = false;
		boolean hasFloatPart = false;
		if (tokenizer.hasNext() && tokenizer.getCurrent() == '.') {
			hasPoint = true;
			tokenizer.next();

			hasFloatPart = visitInteger(tokenizer);
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

			hasMantissa = visitInteger(tokenizer);
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
				tokenizer.error("Long number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
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
					tokenizer.error("Float number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				} else if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				tokenizer.error("Float number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				number = 0;
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
					tokenizer.error("Double number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				} else if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				tokenizer.error("Double number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
				number = 0;
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
				tokenizer.error("Integer number too large", line, offset, tokenizer.getOffset() - offset, lineOffset);
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new IntToken((int) number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		}
	}

	private boolean visitInteger(Tokenizer tokenizer) {
		// decimal or octal (0x)
		boolean found = false;
		if (tokenizer.hasNext() && tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') {
			found = true;
			tokenizer.next();

			boolean lastUnderscore = false;
			while (tokenizer.hasNext() && ((tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') || tokenizer.getCurrent() == '_')) {
				found = true;
				lastUnderscore = tokenizer.getCurrent() == '_';
				tokenizer.next();
			}

			if (lastUnderscore) {
				return false;
			}
		}
		return found;
	}

	public static void main(String[] args) {
		System.out.println(Double.parseDouble(".1E+2"));
		System.out.println(Double.parseDouble(".1E-2"));
		System.out.println(Integer.decode("0xff"));
	}
}
