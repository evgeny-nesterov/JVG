package ru.nest.hiscript.tokenizer;

public class SymbolTokenVisitor implements TokenVisitor {
	@Override
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int offset = tokenizer.getOffset();
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		int type = -1;
		int length = 1;

		char c = tokenizer.getCurrent();
		switch (c) {
			case '(':
				type = Symbols.PARANTHESIS_LEFT;
				break;

			case ')':
				type = Symbols.PARANTHESIS_RIGHT;
				break;

			case '{':
				type = Symbols.BRACES_LEFT;
				break;

			case '}':
				type = Symbols.BRACES_RIGHT;
				break;

			case ';':
				type = Symbols.SEMICOLON;
				break;

			case ':':
				type = Symbols.COLON;
				break;

			case ',':
				type = Symbols.COMMA;
				break;

			case '.':
				if (tokenizer.look_forward() == '.') {
					tokenizer.next();
					if (tokenizer.look_forward() == '.') {
						tokenizer.next();
						type = Symbols.TRIPLEPOINTS;
						length = 3;
					} else {
						return null;
					}
				} else {
					type = Symbols.POINT;
				}
				break;

			case '\'':
				type = Symbols.SINGLE_QUOTE;
				break;

			case '"':
				type = Symbols.DOUBLE_QUOTE;
				break;

			case '\\':
				type = Symbols.BACK_SLASH;
				break;

			case '/':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_DEVIDE;
					length = 2;
				} else {
					type = Symbols.DEVIDE;
				}
				break;

			case '*':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_MULTIPLY;
					length = 2;
				} else {
					type = Symbols.MULTIPLY;
				}
				break;

			case '?':
				type = Symbols.QUESTION;
				break;

			case '!':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.NOT_EQUALS;
					length = 2;
				} else {
					type = Symbols.EXCLAMATION;
				}
				break;

			case '^':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_BITWISE_XOR;
					length = 2;
				} else {
					type = Symbols.BITWISE_XOR;
				}
				break;

			case '%':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_PERCENT;
					length = 2;
				} else {
					type = Symbols.PERCENT;
				}
				break;

			case '+':
				if (tokenizer.look_forward() == '+') {
					tokenizer.next();
					type = Symbols.PLUS_PLUS;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_PLUS;
					length = 2;
				} else {
					type = Symbols.PLUS;
				}
				break;

			case '-':
				if (tokenizer.look_forward() == '-') {
					tokenizer.next();
					type = Symbols.MINUS_MINUS;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_MINUS;
					length = 2;
				} else {
					type = Symbols.MINUS;
				}
				break;

			case '>':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.GREATER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.look_forward() == '>') {
					tokenizer.next();
					if (tokenizer.look_forward() == '=') {
						tokenizer.next();
						type = Symbols.EQUATE_BITWISE_SHIFT_RIGHT;
						length = 3;
					} else {
						type = Symbols.BITWISE_SHIFT_RIGHT;
						length = 2;
					}
				} else {
					type = Symbols.GREATER;
				}
				break;

			case '<':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.LOWER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.look_forward() == '<') {
					tokenizer.next();
					if (tokenizer.look_forward() == '=') {
						tokenizer.next();
						type = Symbols.EQUATE_BITWISE_SHIFT_LEFT;
						length = 3;
					} else {
						type = Symbols.BITWISE_SHIFT_LEFT;
						length = 2;
					}
				} else {
					type = Symbols.LOWER;
				}
				break;

			case '=':
				length = 2;
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUALS;
					length = 2;
				} else {
					type = Symbols.EQUATE;
				}
				break;

			case '&':
				if (tokenizer.look_forward() == '&') {
					tokenizer.next();
					type = Symbols.LOGICAL_AND;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_BITWISE_AND;
					length = 2;
				} else {
					type = Symbols.BITWISE_AND;
				}
				break;

			case '|':
				if (tokenizer.look_forward() == '|') {
					tokenizer.next();
					type = Symbols.LOGICAL_OR;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = Symbols.EQUATE_BITWISE_OR;
					length = 2;
				} else {
					type = Symbols.BITWISE_OR;
				}
				break;

			case '[':
				if (tokenizer.look_forward() == ']') {
					tokenizer.next();
					type = Symbols.MASSIVE;
					length = 2;
				} else {
					type = Symbols.SQUARE_BRACES_LEFT;
				}
				break;

			case ']':
				type = Symbols.SQUARE_BRACES_RIGHT;
				break;
		}
		tokenizer.next();

		if (type != -1) {
			return new SymbolToken(type, line, offset, length, lineOffset);
		} else {
			return null;
		}
	}
}
