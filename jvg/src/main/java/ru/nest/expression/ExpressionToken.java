package ru.nest.expression;

public class ExpressionToken {
	final static int SHIFT = OperationValue.BITS;

	final static int OPERATION = (1 << SHIFT) - 1;

	final static int WORD = 1 << SHIFT;

	final static int NUMBER = 2 << SHIFT;

	final static int PARENTHESIS_OPEN = 3 << SHIFT;

	final static int PARENTHESIS_CLOSE = 4 << SHIFT;

	final static int QUESTION = 5 << SHIFT;

	final static int COLON = 6 << SHIFT;

	final static int COMMA = 7 << SHIFT;

	final static int EXCLAMATION = 8 << SHIFT;

	public ExpressionToken(int type, String token) {
		this.type = type;
		this.token = token;
	}

	private int type;

	public int getType() {
		return type;
	}

	private String token;

	public String getToken() {
		return token;
	}

	public boolean isOperation() {
		return (type & OPERATION) != 0;
	}

	public static boolean isOperation(int type) {
		return (type & OPERATION) != 0;
	}

	@Override
	public String toString() {
		String type = "";
		if (this.type == WORD) {
			type = "word";
		} else if (this.type == NUMBER) {
			type = "number";
		} else if (this.type == PARENTHESIS_OPEN) {
			type = "block open";
		} else if (this.type == PARENTHESIS_CLOSE) {
			type = "block close";
		} else if (isOperation()) {
			type = "operation";
		}

		return type + (token != null ? (": " + token) : "") + ", type=" + this.type;
	}
}
