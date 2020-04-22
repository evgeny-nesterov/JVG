package ru.nest.hiscript.tokenizer;

public class NumberTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int offset = tokenizer.getOffset();
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		boolean hasIntegerPart = visitInteger(tokenizer);

		boolean hasPoint = false;
		boolean hasFloatPart = false;
		if (tokenizer.hasNext() && tokenizer.getCurrent() == '.') {
			hasPoint = true;
			tokenizer.next();

			hasFloatPart = visitInteger(tokenizer);
		}

		if ((!hasIntegerPart && !hasPoint) || (hasPoint && !hasIntegerPart && !hasFloatPart)) {
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
			return null;
		}

		boolean isFloat = false;
		boolean isLong = false;
		if (tokenizer.hasNext() && (tokenizer.getCurrent() == 'f' || tokenizer.getCurrent() == 'F')) {
			isFloat = true;
			tokenizer.next();
		} else if (!hasPoint && !hasExponent && tokenizer.hasNext() && (tokenizer.getCurrent() == 'l' || tokenizer.getCurrent() == 'L')) {
			isLong = true;
			tokenizer.next();
		}

		String text = tokenizer.getText(offset, tokenizer.getOffset() - (isLong || isFloat ? 1 : 0));
		double number;
		try {
			number = Double.parseDouble(text);
		} catch (NumberFormatException exc) {
			exc.printStackTrace();
			return null;
		}

		if (isLong) {
			return new LongToken((long) number, line, offset, tokenizer.getOffset() - offset, lineOffset);
		} else if (isFloat) {
			return new FloatToken((float) number, line, offset, tokenizer.getOffset() - offset, lineOffset);
		} else if (!hasPoint && !hasExponent) {
			if (number >= Byte.MIN_VALUE && number <= Byte.MAX_VALUE) {
				return new ByteToken((byte) number, line, offset, tokenizer.getOffset() - offset, lineOffset);
			} else if (number >= Short.MIN_VALUE && number <= Short.MAX_VALUE) {
				return new ShortToken((short) number, line, offset, tokenizer.getOffset() - offset, lineOffset);
			} else {
				return new IntToken((int) number, line, offset, tokenizer.getOffset() - offset, lineOffset);
			}
		} else {
			return new DoubleToken(number, line, offset, tokenizer.getOffset() - offset, lineOffset);
		}
	}

	private boolean visitInteger(Tokenizer tokenizer) {
		boolean found = false;
		while (tokenizer.hasNext() && tokenizer.getCurrent() >= '0' && tokenizer.getCurrent() <= '9') {
			found = true;
			tokenizer.next();
		}
		return found;
	}

	public static void main(String[] args) {
		System.out.println(Double.parseDouble("+.1E+2"));
	}
}
