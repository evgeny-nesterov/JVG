package script.tokenizer;

public class SymbolTokenVisitor implements TokenVisitor {
	public Token getToken(Tokenizer tokenizer) throws TokenizerException {
		int offset = tokenizer.getOffset();
		int line = tokenizer.getLine();
		int lineOffset = tokenizer.getLineOffset();

		int type = -1;
		int length = 1;

		char c = tokenizer.getCurrent();
		switch (c) {
			case '(':
				type = SymbolToken.PARANTHESIS_LEFT;
				break;

			case ')':
				type = SymbolToken.PARANTHESIS_RIGHT;
				break;

			case '{':
				type = SymbolToken.BRACES_LEFT;
				break;

			case '}':
				type = SymbolToken.BRACES_RIGHT;
				break;

			case ';':
				type = SymbolToken.SEMICOLON;
				break;

			case ':':
				type = SymbolToken.COLON;
				break;

			case ',':
				type = SymbolToken.COMMA;
				break;

			case '.':
				type = SymbolToken.POINT;
				break;

			case '\'':
				type = SymbolToken.SINGLE_QUOTE;
				break;

			case '"':
				type = SymbolToken.DOUBLE_QUOTE;
				break;

			case '\\':
				type = SymbolToken.BACK_SLASH;
				break;

			case '/':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_DEVIDE;
					length = 2;
				} else {
					type = SymbolToken.DEVIDE;
				}
				break;

			case '*':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_MULTIPLY;
					length = 2;
				} else {
					type = SymbolToken.MULTIPLY;
				}
				break;

			case '?':
				type = SymbolToken.QUESTION;
				break;

			case '!':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.NOT_EQUALS;
					length = 2;
				} else {
					type = SymbolToken.EXCLAMATION;
				}
				break;

			case '^':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_BITWISE_XOR;
					length = 2;
				} else {
					type = SymbolToken.BITWISE_XOR;
				}
				break;

			case '%':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_PERCENT;
					length = 2;
				} else {
					type = SymbolToken.PERCENT;
				}
				break;

			case '+':
				if (tokenizer.look_forward() == '+') {
					tokenizer.next();
					type = SymbolToken.PLUS_PLUS;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_PLUS;
					length = 2;
				} else {
					type = SymbolToken.PLUS;
				}
				break;

			case '-':
				if (tokenizer.look_forward() == '-') {
					tokenizer.next();
					type = SymbolToken.MINUS_MINUS;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_MINUS;
					length = 2;
				} else {
					type = SymbolToken.MINUS;
				}
				break;

			case '>':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.GREATER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.look_forward() == '>') {
					tokenizer.next();
					if (tokenizer.look_forward() == '=') {
						tokenizer.next();
						type = SymbolToken.EQUATE_BITWISE_SHIFT_RIGHT;
						length = 3;
					} else {
						type = SymbolToken.BITWISE_SHIFT_RIGHT;
						length = 2;
					}
				} else {
					type = SymbolToken.GREATER;
				}
				break;

			case '<':
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.LOWER_OR_EQUAL;
					length = 2;
				} else if (tokenizer.look_forward() == '<') {
					tokenizer.next();
					if (tokenizer.look_forward() == '=') {
						tokenizer.next();
						type = SymbolToken.EQUATE_BITWISE_SHIFT_LEFT;
						length = 3;
					} else {
						type = SymbolToken.BITWISE_SHIFT_LEFT;
						length = 2;
					}
				} else {
					type = SymbolToken.LOWER;
				}
				break;

			case '=':
				length = 2;
				if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUALS;
					length = 2;
				} else {
					type = SymbolToken.EQUATE;
				}
				break;

			case '&':
				if (tokenizer.look_forward() == '&') {
					tokenizer.next();
					type = SymbolToken.LOGICAL_AND;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_BITWISE_AND;
					length = 2;
				} else {
					type = SymbolToken.BITWISE_AND;
				}
				break;

			case '|':
				if (tokenizer.look_forward() == '|') {
					tokenizer.next();
					type = SymbolToken.LOGICAL_OR;
					length = 2;
				} else if (tokenizer.look_forward() == '=') {
					tokenizer.next();
					type = SymbolToken.EQUATE_BITWISE_OR;
					length = 2;
				} else {
					type = SymbolToken.BITWISE_OR;
				}
				break;

			case '[':
				if (tokenizer.look_forward() == ']') {
					tokenizer.next();
					type = SymbolToken.MASSIVE;
					length = 2;
				} else {
					type = SymbolToken.SQUARE_BRACES_LEFT;
				}
				break;

			case ']':
				type = SymbolToken.SQUARE_BRACES_RIGHT;
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
