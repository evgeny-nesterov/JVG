package ru.nest.hiscript.tokenizer;

public class OperationSymbols implements Symbols {
	public static boolean isOperation(int type) {
		switch (type) {
			case GREATER:
			case GREATER_OR_EQUAL:
			case LOWER:
			case LOWER_OR_EQUAL:
			case EQUALS:
			case NOT_EQUALS:
			case LOGICAL_AND:
			case LOGICAL_OR:

			case PLUS:
			case MINUS:
			case MULTIPLY:
			case DIVIDE:
			case BITWISE_AND:
			case BITWISE_OR:
			case BITWISE_SHIFT_LEFT:
			case BITWISE_SHIFT_RIGHT:
			case BITWISE_SHIFT_RIGHT_CYCLIC:
			case BITWISE_XOR:
			case PERCENT:

			case EQUATE:
			case EQUATE_PLUS:
			case EQUATE_MINUS:
			case EQUATE_MULTIPLY:
			case EQUATE_DIVIDE:
			case EQUATE_PERCENT:
			case EQUATE_BITWISE_AND:
			case EQUATE_BITWISE_OR:
			case EQUATE_BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_BITWISE_XOR:

			case POINT:
				return true;
		}
		return false;
	}

	public static boolean isPrefixOperation(int type) {
		switch (type) {
			case PLUS:
			case PLUS_PLUS:
			case MINUS:
			case MINUS_MINUS:
			case EXCLAMATION:
			case TILDA:
				return true;
		}
		return false;
	}

	public static int getPriority(int type) {
		switch (type) {
			case EQUATE:
			case EQUATE_PLUS:
			case EQUATE_MINUS:
			case EQUATE_MULTIPLY:
			case EQUATE_DIVIDE:
			case EQUATE_PERCENT:
			case EQUATE_BITWISE_AND:
			case EQUATE_BITWISE_OR:
			case EQUATE_BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_BITWISE_XOR:
				return 300;

			case DIVIDE:
				return 204;
			case MULTIPLY:
				return 203;
			case PERCENT:
				return 202;
			case BITWISE_AND:
			case BITWISE_OR:
			case BITWISE_SHIFT_LEFT:
			case BITWISE_SHIFT_RIGHT:
			case BITWISE_SHIFT_RIGHT_CYCLIC:
			case BITWISE_XOR:
				return 201;
			case PLUS:
			case MINUS:
				return 200;

			case GREATER:
			case GREATER_OR_EQUAL:
			case LOWER:
			case LOWER_OR_EQUAL:
			case EQUALS:
			case NOT_EQUALS:
				return 100;

			case LOGICAL_AND:
			case LOGICAL_OR:
				return 0;
		}
		return 0;
	}

	public static boolean isEquate(int operation) {
		switch (operation) {
			case EQUATE:
			case EQUATE_BITWISE_AND:
			case EQUATE_BITWISE_OR:
			case EQUATE_BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_BITWISE_XOR:
			case EQUATE_DIVIDE:
			case EQUATE_MINUS:
			case EQUATE_MULTIPLY:
			case EQUATE_PERCENT:
			case EQUATE_PLUS:
				return true;
		}
		return false;
	}
}
