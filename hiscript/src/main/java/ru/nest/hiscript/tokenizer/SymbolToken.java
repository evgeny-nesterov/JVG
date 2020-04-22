package ru.nest.hiscript.tokenizer;

public class SymbolToken extends Token implements Symbols {
	public SymbolToken(int type, int line, int offset, int length, int lineOffset) {
		super(line, offset, length, lineOffset);
		this.type = type;
	}

	private int type;

	public int getType() {
		return type;
	}

	public static String getSymbol(int type) {
		switch (type) {
			case BACK_SLASH:
				return "\\";

			case BITWISE_AND:
				return "&";

			case BITWISE_OR:
				return "|";

			case BITWISE_SHIFT_LEFT:
				return "<<";

			case BITWISE_SHIFT_RIGHT:
				return ">>";

			case BITWISE_XOR:
				return "^";

			case BRACES_LEFT:
				return "{";

			case BRACES_RIGHT:
				return "}";

			case COMMA:
				return ",";

			case DEVIDE:
				return "/";

			case DOUBLE_QUOTE:
				return "\"";

			case EQUALS:
				return "==";

			case EQUATE:
				return "=";

			case EQUATE_DEVIDE:
				return "/=";

			case EQUATE_BITWISE_AND:
				return "&=";

			case EQUATE_BITWISE_OR:
				return "|=";

			case EQUATE_BITWISE_SHIFT_LEFT:
				return "<<=";

			case EQUATE_BITWISE_SHIFT_RIGHT:
				return ">>=";

			case EQUATE_BITWISE_XOR:
				return "^=";

			case EQUATE_MINUS:
				return "-=";

			case EQUATE_MULTIPLY:
				return "*=";

			case EQUATE_PLUS:
				return "+=";

			case EQUATE_PERCENT:
				return "%";

			case EXCLAMATION:
				return "!";

			case GREATER:
				return ">";

			case GREATER_OR_EQUAL:
				return ">=";

			case LOGICAL_AND:
				return "&&";

			case LOGICAL_OR:
				return "||";

			case LOWER:
				return "<";

			case LOWER_OR_EQUAL:
				return "<=";

			case MINUS:
				return "-";

			case MINUS_MINUS:
				return "--";

			case MULTIPLY:
				return "*";

			case NOT_EQUALS:
				return "!=";

			case PARANTHESIS_LEFT:
				return "(";

			case PARANTHESIS_RIGHT:
				return ")";

			case PERCENT:
				return "%";

			case PLUS:
				return "+";

			case PLUS_PLUS:
				return "++";

			case POINT:
				return ".";

			case QUESTION:
				return "?";

			case SEMICOLON:
				return ";";

			case SINGLE_QUOTE:
				return "'";

			case COLON:
				return ":";

			case SQUARE_BRACES_LEFT:
				return "[";

			case SQUARE_BRACES_RIGHT:
				return "]";

			case MASSIVE:
				return "[]";
		}

		return "";
	}

	public String getSymbol() {
		return getSymbol(type);
	}

	@Override
	public String toString() {
		return "Symbol [" + getSymbol() + ", " + super.toString() + "]";
	}

	public static boolean isOperation(int type) {
		return false;
	}
}
