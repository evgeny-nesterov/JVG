package ru.nest.hiscript.tokenizer;

public class SymbolTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) {
		int offset = tokenizer.getOffset();
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		SymbolType type = null;
		int length = 1;

		char c = tokenizer.getCurrent();
		switch (c) {
			case '(':
				type = SymbolType.PARENTHESES_LEFT;
				break;

			case ')':
				type = SymbolType.PARENTHESES_RIGHT;
				break;

			case '{':
				type = SymbolType.BRACES_LEFT;
				break;

			case '}':
				type = SymbolType.BRACES_RIGHT;
				break;

			case ';':
				type = SymbolType.SEMICOLON;
				break;

			case ':':
				if (tokenizer.lookForward() == ':') {
					tokenizer.next();
					type = SymbolType.DOUBLE_COLON;
					length = 2;
				} else {
					type = SymbolType.COLON;
				}
				break;

			case ',':
				type = SymbolType.COMMA;
				break;

			case '.':
				if (tokenizer.lookForward() == '.') {
					tokenizer.next();
					if (tokenizer.lookForward() == '.') {
						tokenizer.next();
						type = SymbolType.TRIPLE_POINTS;
						length = 3;
					} else {
						return null;
					}
				} else {
					type = SymbolType.POINT;
				}
				break;

			case '\'':
				type = SymbolType.SINGLE_QUOTE;
				break;

			case '\\':
				type = SymbolType.BACK_SLASH;
				break;

			case '/':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_DIVIDE;
					length = 2;
				} else {
					type = SymbolType.DIVIDE;
				}
				break;

			case '*':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_MULTIPLY;
					length = 2;
				} else {
					type = SymbolType.MULTIPLY;
				}
				break;

			case '?':
				type = SymbolType.QUESTION;
				break;

			case '!':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.NOT_EQUALS;
					length = 2;
				} else {
					type = SymbolType.EXCLAMATION;
				}
				break;

			case '^':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_BITWISE_XOR;
					length = 2;
				} else {
					type = SymbolType.BITWISE_XOR;
				}
				break;

			case '%':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_PERCENT;
					length = 2;
				} else {
					type = SymbolType.PERCENT;
				}
				break;

			case '+':
				if (tokenizer.lookForward() == '+') {
					tokenizer.next();
					type = SymbolType.PLUS_PLUS;
					length = 2;
				} else if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_PLUS;
					length = 2;
				} else {
					type = SymbolType.PLUS;
				}
				break;

			case '-':
				if (tokenizer.lookForward() == '-') {
					tokenizer.next();
					type = SymbolType.MINUS_MINUS;
					length = 2;
				} else if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_MINUS;
					length = 2;
				} else if (tokenizer.lookForward() == '>') {
					tokenizer.next();
					type = SymbolType.REFERENCE;
					length = 2;
				} else {
					type = SymbolType.MINUS;
				}
				break;

			case '>':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.GREATER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.lookForward() == '>') {
					tokenizer.next();
					boolean cyclic = false;
					if (tokenizer.lookForward() == '>') {
						tokenizer.next();
						cyclic = true;
					}
					if (tokenizer.lookForward() == '=') {
						tokenizer.next();
						type = cyclic ? SymbolType.EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC : SymbolType.EQUATE_BITWISE_SHIFT_RIGHT;
						length = 3;
					} else {
						type = cyclic ? SymbolType.BITWISE_SHIFT_RIGHT_CYCLIC : SymbolType.BITWISE_SHIFT_RIGHT;
						length = 2;
					}
				} else {
					type = SymbolType.GREATER;
				}
				break;

			case '<':
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.LOWER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.lookForward() == '<') {
					tokenizer.next();
					if (tokenizer.lookForward() == '=') {
						tokenizer.next();
						type = SymbolType.EQUATE_BITWISE_SHIFT_LEFT;
						length = 3;
					} else {
						type = SymbolType.BITWISE_SHIFT_LEFT;
						length = 2;
					}
				} else {
					type = SymbolType.LOWER;
				}
				break;

			case '=':
				length = 2;
				if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUALS;
					length = 2;
				} else {
					type = SymbolType.EQUATE;
				}
				break;

			case '&':
				if (tokenizer.lookForward() == '&') {
					tokenizer.next();
					type = SymbolType.LOGICAL_AND;
					length = 2;
				} else if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_BITWISE_AND;
					length = 2;
				} else {
					type = SymbolType.BITWISE_AND;
				}
				break;

			case '|':
				if (tokenizer.lookForward() == '|') {
					tokenizer.next();
					type = SymbolType.LOGICAL_OR;
					length = 2;
				} else if (tokenizer.lookForward() == '=') {
					tokenizer.next();
					type = SymbolType.EQUATE_BITWISE_OR;
					length = 2;
				} else {
					type = SymbolType.BITWISE_OR;
				}
				break;

			case '[':
				if (tokenizer.lookForward() == ']') {
					tokenizer.next();
					type = SymbolType.MASSIVE;
					length = 2;
				} else {
					type = SymbolType.SQUARE_BRACES_LEFT;
				}
				break;

			case ']':
				type = SymbolType.SQUARE_BRACES_RIGHT;
				break;

			case '~':
				type = SymbolType.TILDA;
				break;
		}
		tokenizer.next();

		if (type != null) {
			return new SymbolToken(type, line, offset, length, lineOffset);
		} else {
			return null;
		}
	}
}
