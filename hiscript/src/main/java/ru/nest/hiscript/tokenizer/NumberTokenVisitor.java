package ru.nest.hiscript.tokenizer;

public class NumberTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		boolean negative = false;
		boolean hasSign = false;
		if (tokenizer.hasNext() && (tokenizer.getCurrent() == '-' || tokenizer.getCurrent() == '+')) {
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
			hasSign = true;
		}

		int offset = tokenizer.getOffset();
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
				if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				if (hasSign) {
					tokenizer.rollback();
				}
				return null;
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
					if (hasSign) {
						tokenizer.rollback();
					}
					return null;
				}
				if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				if (hasSign) {
					tokenizer.rollback();
				}
				return null;
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
					if (hasSign) {
						tokenizer.rollback();
					}
					return null;
				}
				if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				if (hasSign) {
					tokenizer.rollback();
				}
				return null;
			}
			if (hasSign) {
				tokenizer.commit();
			}
			return new DoubleToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
		} else {
			int number;
			try {
				number = Integer.parseInt(text);
				if (negative) {
					number = -number;
				}
			} catch (NumberFormatException exc) {
				if (hasSign) {
					tokenizer.rollback();
				}
				return null;
			}
			if (hasSign) {
				tokenizer.commit();
			}
			if (number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE) {
				return new ByteToken((byte) number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			} else if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
				return new ShortToken((short) number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			} else {
				return new IntToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset, hasSign);
			}
		}
	}

	private boolean visitInteger(Tokenizer tokenizer) {
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
	}
}
