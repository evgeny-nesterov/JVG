package ru.nest.expression;

public class ExpressionTokenizer {
	public ExpressionTokenizer() {
	}

	public ExpressionTokenizer(String expression) {
		init(expression);
	}

	public void init(String expression) {
		c = expression.toCharArray();
		isEnd = false;
		pos = -1;
		len = c.length;
		next_char();
	}

	private char[] c;

	private int len;

	private int pos = -1;

	private boolean isEnd;

	private char cur;

	private ExpressionToken token;

	private void next_char() {
		if (!isEnd) {
			pos++;
			isEnd = pos == len;
			if (!isEnd) {
				cur = c[pos];
			} else {
				cur = 0;
			}
		} else {
			cur = 0;
		}
	}

	private char forward() {
		if (pos < len - 1) {
			return c[pos + 1];
		} else {
			return 0;
		}
	}

	private void omitWhitespaces() {
		while (!isEnd && Character.isWhitespace(cur)) {
			next_char();
		}
	}

	public boolean hasNext() {
		return !isEnd;
	}

	public ExpressionToken next() throws Exception {
		omitWhitespaces();

		if (searchNumber() || searchOperation() || searchSimbol() || searchWord()) {
			return token;
		}

		if (!isEnd) {
			throw new Exception("Illegal symbol '" + cur + "' at " + pos);
		}

		return null;
	}

	private boolean searchWord() {
		if (Character.isLetter(cur) || cur == '_' || cur == '$') {
			buf.delete(0, buf.length());
			while (Character.isLetter(cur) || Character.isDigit(cur) || cur == '_' || cur == '$') {
				buf.append(cur);
				next_char();
			}

			token = new ExpressionToken(ExpressionToken.WORD, buf.toString());
			return true;
		} else {
			return false;
		}
	}

	private boolean searchSimbol() {
		int type = -1;
		switch (cur) {
			case '(':
				type = ExpressionToken.PARENTHESIS_OPEN;
				break;

			case ')':
				type = ExpressionToken.PARENTHESIS_CLOSE;
				break;

			case '?':
				type = ExpressionToken.QUESTION;
				break;

			case ':':
				type = ExpressionToken.COLON;
				break;

			case ',':
				type = ExpressionToken.COMMA;
				break;

			case '!':
				type = ExpressionToken.EXCLAMATION;
				break;
		}

		if (type != -1) {
			token = new ExpressionToken(type, null);
			next_char();
			return true;
		}

		return false;
	}

	private StringBuffer buf = new StringBuffer();

	private boolean searchNumber() {
		int old = pos;
		buf.delete(0, buf.length());

		boolean wasPoint = false;
		boolean wasDigit = false;
		while (true) {
			if (Character.isDigit(cur)) {
				wasDigit = true;
				buf.append(cur);
				next_char();
				continue;
			} else if (!wasPoint && cur == '.') {
				wasPoint = true;
				buf.append(cur);
				next_char();
				continue;
			}

			break;
		}

		if (wasDigit) {
			token = new ExpressionToken(ExpressionToken.NUMBER, buf.toString());
			return true;
		} else {
			pos = old;
			if (!isEnd && pos != -1) {
				cur = c[pos];
			} else {
				cur = 0;
			}
			return false;
		}
	}

	private boolean searchOperation() {
		int operationType;
		switch (cur) {
			case '+':
				operationType = OperationValue.PLUS;
				break;

			case '-':
				operationType = OperationValue.MINUS;
				break;

			case '*':
				operationType = OperationValue.MULT;
				break;

			case '/':
				operationType = OperationValue.DIV;
				break;

			case '|':
				char next = forward();
				if (next == '|') {
					next_char();
					operationType = OperationValue.OR;
				} else {
					operationType = OperationValue.BINARY_OR;
				}
				break;

			case '&':
				next = forward();
				if (next == '&') {
					next_char();
					operationType = OperationValue.AND;
				} else {
					operationType = OperationValue.BINARY_AND;
				}
				break;

			case '^':
				operationType = OperationValue.BINARY_EXCLUSIVE_OR;
				break;

			case '=':
				next = forward();
				if (next == '=') {
					next_char();
					operationType = OperationValue.EQUAL;
					break;
				} else {
					return false;
				}

			case '!':
				next = forward();
				if (next == '=') {
					next_char();
					operationType = OperationValue.NOT_EQUAL;
					break;
				} else {
					return false;
				}

			case '%':
				operationType = OperationValue.RESIDUE;
				break;

			case '>':
				next = forward();
				if (next == '=') {
					next_char();
					operationType = OperationValue.GREATER_OR_EQUAL;
				} else if (next == '>') {
					next_char();
					operationType = OperationValue.BINARY_SHIFT_RIGHT;
				} else {
					operationType = OperationValue.GREATER;
				}
				break;

			case '<':
				next = forward();
				if (next == '=') {
					next_char();
					operationType = OperationValue.LESS_OR_EQUAL;
				} else if (next == '<') {
					next_char();
					operationType = OperationValue.BINARY_SHIFT_LEFT;
				} else {
					operationType = OperationValue.LESS;
				}
				break;

			default:
				return false;
		}
		next_char();

		token = new ExpressionToken(operationType, null);
		return true;
	}
}
