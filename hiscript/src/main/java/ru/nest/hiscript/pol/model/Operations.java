package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.OperationSymbols;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

import static ru.nest.hiscript.tokenizer.SymbolType.MINUS;

public class Operations extends OperationSymbols implements Words {
	public static void doOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		if (left.type == VOID || right.type == VOID) {
			throw new ExecuteException("void type not allowed here");
		} else if ((!left.isArray() && left.type == STRING) || (!right.isArray() && right.type == STRING)) {
			doStringOperation(left, right, operation);
			return;
		} else if (left.isArray() || right.isArray()) {
			doArrayOperation(left, right, operation);
			return;
		} else if (left.type == BOOLEAN || right.type == BOOLEAN) {
			doBooleanOperation(left, right, operation);
			return;
		}

		switch (left.type) {
			case DOUBLE:
				doDoubleOperation(left, right, operation);
				break;

			case FLOAT:
				switch (right.type) {
					case DOUBLE:
						doDoubleOperation(left, right, operation);
						break;

					default:
						doFloatOperation(left, right, operation);
				}
				break;

			case LONG:
				switch (right.type) {
					case DOUBLE:
						doDoubleOperation(left, right, operation);
						break;

					case FLOAT:
						doFloatOperation(left, right, operation);
						break;

					default:
						doLongOperation(left, right, operation);
				}
				break;

			default:
				switch (right.type) {
					case DOUBLE:
						doDoubleOperation(left, right, operation);
						break;

					case FLOAT:
						doFloatOperation(left, right, operation);
						break;

					case LONG:
						doLongOperation(left, right, operation);
						break;

					default:
						doIntOperation(left, right, operation);
				}
		}
	}

	public static void doArrayOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		if (left.dimension == right.dimension && left.type == right.type) {
			switch (operation) {
				case EQUATE:
					left.array = right.array;
					return;
			}
		} else if (left.dimension > 0 && right.dimension > 0) {
			switch (operation) {
				case EQUALS:
					left.bool = left.array == right.array;
					left.type = BOOLEAN;
					return;

				case NOT_EQUALS:
					left.bool = left.array != right.array;
					left.type = BOOLEAN;
					return;
			}
		}

		throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + left.getTypeDescr() + ", " + right.getTypeDescr());
	}

	public static void doStringOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case PLUS:
			case EQUATE_PLUS:
				left.string = left.convertToString() + right.convertToString();
				left.type = STRING;
				break;

			case EQUATE:
				left.castString();
				left.string = right.convertToString();
				left.type = STRING;
				break;

			case EQUALS:
				left.bool = left.convertToString() == right.convertToString();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.convertToString() != right.convertToString();
				left.type = BOOLEAN;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doBooleanOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case LOGICAL_AND:
				left.bool = left.getBoolean() && right.getBoolean();
				left.type = BOOLEAN;
				break;

			case LOGICAL_OR:
				left.bool = left.getBoolean() || right.getBoolean();
				left.type = BOOLEAN;
				break;

			case BITWISE_AND:
			case EQUATE_BITWISE_AND:
				left.bool = left.getBoolean() & right.getBoolean();
				left.type = BOOLEAN;
				break;

			case BITWISE_OR:
			case EQUATE_BITWISE_OR:
				left.bool = left.getBoolean() | right.getBoolean();
				left.type = BOOLEAN;
				break;

			case BITWISE_XOR:
			case EQUATE_BITWISE_XOR:
				left.bool = left.getBoolean() ^ right.getBoolean();
				left.type = BOOLEAN;
				break;

			case EQUATE:
				left.castBoolean();
				left.bool = right.getBoolean();
				left.type = BOOLEAN;
				break;

			case EQUALS:
				left.bool = left.getBoolean() == right.getBoolean();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.getBoolean() != right.getBoolean();
				left.type = BOOLEAN;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doDoubleOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case GREATER:
				left.bool = left.getDouble() > right.getDouble();
				left.type = BOOLEAN;
				break;

			case GREATER_OR_EQUAL:
				left.bool = left.getDouble() >= right.getDouble();
				left.type = BOOLEAN;
				break;

			case LOWER:
				left.bool = left.getDouble() < right.getDouble();
				left.type = BOOLEAN;
				break;

			case LOWER_OR_EQUAL:
				left.bool = left.getDouble() <= right.getDouble();
				left.type = BOOLEAN;
				break;

			case EQUALS:
				left.bool = left.getDouble() == right.getDouble();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.getDouble() != right.getDouble();
				left.type = BOOLEAN;
				break;

			case PLUS:
			case EQUATE_PLUS:
				left.doubleNumber = left.getDouble() + right.getDouble();
				left.type = DOUBLE;
				break;

			case MINUS:
			case EQUATE_MINUS:
				left.doubleNumber = left.getDouble() - right.getDouble();
				left.type = DOUBLE;
				break;

			case MULTIPLY:
			case EQUATE_MULTIPLY:
				left.doubleNumber = left.getDouble() * right.getDouble();
				left.type = DOUBLE;
				break;

			case DIVIDE:
			case EQUATE_DIVIDE:
				left.doubleNumber = left.getDouble() / right.getDouble();
				left.type = DOUBLE;
				break;

			case PERCENT:
			case EQUATE_PERCENT:
				left.doubleNumber = left.getDouble() % right.getDouble();
				left.type = DOUBLE;
				break;

			case EQUATE:
				left.castDouble();
				left.doubleNumber = right.getDouble();
				left.type = DOUBLE;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doFloatOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case GREATER:
				left.bool = left.getFloat() > right.getFloat();
				left.type = BOOLEAN;
				break;

			case GREATER_OR_EQUAL:
				left.bool = left.getFloat() >= right.getFloat();
				left.type = BOOLEAN;
				break;

			case LOWER:
				left.bool = left.getFloat() < right.getFloat();
				left.type = BOOLEAN;
				break;

			case LOWER_OR_EQUAL:
				left.bool = left.getFloat() <= right.getFloat();
				left.type = BOOLEAN;
				break;

			case EQUALS:
				left.bool = left.getFloat() == right.getFloat();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.getFloat() != right.getFloat();
				left.type = BOOLEAN;
				break;

			case PLUS:
			case EQUATE_PLUS:
				left.floatNumber = left.getFloat() + right.getFloat();
				left.type = FLOAT;
				break;

			case MINUS:
			case EQUATE_MINUS:
				left.floatNumber = left.getFloat() - right.getFloat();
				left.type = FLOAT;
				break;

			case MULTIPLY:
			case EQUATE_MULTIPLY:
				left.floatNumber = left.getFloat() * right.getFloat();
				left.type = FLOAT;
				break;

			case DIVIDE:
			case EQUATE_DIVIDE:
				left.floatNumber = left.getFloat() / right.getFloat();
				left.type = FLOAT;
				break;

			case PERCENT:
			case EQUATE_PERCENT:
				left.floatNumber = left.getFloat() % right.getFloat();
				left.type = FLOAT;
				break;

			case EQUATE:
				left.castFloat();
				left.floatNumber = right.getFloat();
				left.type = FLOAT;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doLongOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case GREATER:
				left.bool = left.getLong() > right.getLong();
				left.type = BOOLEAN;
				break;

			case GREATER_OR_EQUAL:
				left.bool = left.getLong() >= right.getLong();
				left.type = BOOLEAN;
				break;

			case LOWER:
				left.bool = left.getLong() < right.getLong();
				left.type = BOOLEAN;
				break;

			case LOWER_OR_EQUAL:
				left.bool = left.getLong() <= right.getLong();
				left.type = BOOLEAN;
				break;

			case EQUALS:
				left.bool = left.getLong() == right.getLong();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.getLong() != right.getLong();
				left.type = BOOLEAN;
				break;

			case PLUS:
			case EQUATE_PLUS:
				left.longNumber = left.getLong() + right.getLong();
				left.type = LONG;
				break;

			case MINUS:
			case EQUATE_MINUS:
				left.longNumber = left.getLong() - right.getLong();
				left.type = LONG;
				break;

			case MULTIPLY:
			case EQUATE_MULTIPLY:
				left.longNumber = left.getLong() * right.getLong();
				left.type = LONG;
				break;

			case DIVIDE:
			case EQUATE_DIVIDE:
				left.longNumber = left.getLong() / right.getLong();
				left.type = LONG;
				break;

			case BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_LEFT:
				left.longNumber = left.getLong() << right.getLong();
				left.type = LONG;
				break;

			case BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
				left.longNumber = left.getLong() >> right.getLong();
				left.type = LONG;
				break;

			case BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				left.longNumber = left.getLong() >>> right.getLong();
				left.type = LONG;
				break;

			case BITWISE_AND:
			case EQUATE_BITWISE_AND:
				left.longNumber = left.getLong() & right.getLong();
				left.type = LONG;
				break;

			case BITWISE_OR:
			case EQUATE_BITWISE_OR:
				left.longNumber = left.getLong() | right.getLong();
				left.type = LONG;
				break;

			case BITWISE_XOR:
			case EQUATE_BITWISE_XOR:
				left.longNumber = left.getLong() ^ right.getLong();
				left.type = LONG;
				break;

			case PERCENT:
			case EQUATE_PERCENT:
				left.longNumber = left.getLong() % right.getLong();
				left.type = LONG;
				break;

			case EQUATE:
				left.castLong();
				left.longNumber = right.getLong();
				left.type = LONG;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doIntOperation(ValueContainer left, ValueContainer right, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case GREATER:
				left.bool = left.getInt() > right.getInt();
				left.type = BOOLEAN;
				break;

			case GREATER_OR_EQUAL:
				left.bool = left.getInt() >= right.getInt();
				left.type = BOOLEAN;
				break;

			case LOWER:
				left.bool = left.getInt() < right.getInt();
				left.type = BOOLEAN;
				break;

			case LOWER_OR_EQUAL:
				left.bool = left.getInt() <= right.getInt();
				left.type = BOOLEAN;
				break;

			case EQUALS:
				left.bool = left.getInt() == right.getInt();
				left.type = BOOLEAN;
				break;

			case NOT_EQUALS:
				left.bool = left.getInt() != right.getInt();
				left.type = BOOLEAN;
				break;

			case PLUS:
			case EQUATE_PLUS:
				left.intNumber = left.getInt() + right.getInt();
				left.type = INT;
				break;

			case MINUS:
			case EQUATE_MINUS:
				left.intNumber = left.getInt() - right.getInt();
				left.type = INT;
				break;

			case MULTIPLY:
			case EQUATE_MULTIPLY:
				left.intNumber = left.getInt() * right.getInt();
				left.type = INT;
				break;

			case DIVIDE:
			case EQUATE_DIVIDE:
				int a = right.getInt();
				if (a != 0) {
					left.intNumber = left.getInt() / a;
				} else {
					throw new ExecuteException("division by zero");
				}
				left.type = INT;
				break;

			case BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_LEFT:
				left.intNumber = left.getInt() << right.getInt();
				left.type = INT;
				break;

			case BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
				left.intNumber = left.getInt() >> right.getInt();
				left.type = INT;
				break;

			case BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				left.intNumber = left.getInt() >>> right.getInt();
				left.type = INT;
				break;

			case BITWISE_AND:
			case EQUATE_BITWISE_AND:
				left.intNumber = left.getInt() & right.getInt();
				left.type = INT;
				break;

			case BITWISE_OR:
			case EQUATE_BITWISE_OR:
				left.intNumber = left.getInt() | right.getInt();
				left.type = INT;
				break;

			case BITWISE_XOR:
			case EQUATE_BITWISE_XOR:
				left.intNumber = left.getInt() ^ right.getInt();
				left.type = INT;
				break;

			case PERCENT:
			case EQUATE_PERCENT:
				left.intNumber = left.getInt() % right.getInt();
				left.type = INT;
				break;

			case EQUATE:
				left.castInt();
				left.intNumber = right.getInt();
				left.type = INT;
				break;

			default:
				throw new ExecuteException("operator '" + SymbolToken.getSymbol(operation) + "' can not be applied to " + WordToken.getWord(left.type) + ", " + WordToken.getWord(right.type));
		}
	}

	public static void doPrefixOperation(ValueContainer value, SymbolType operation) throws ExecuteException {
		switch (operation) {
			case PLUS:
			case MINUS:
				switch (value.type) {
					case DOUBLE:
						if (operation == MINUS) {
							value.doubleNumber = -value.doubleNumber;
						}
						break;

					case FLOAT:
						if (operation == MINUS) {
							value.floatNumber = -value.floatNumber;
						}
						break;

					case INT:
						if (operation == MINUS) {
							value.intNumber = -value.intNumber;
						}
						break;

					default:
						value.castInt();
						if (operation == MINUS) {
							value.intNumber = -value.intNumber;
						}
						break;
				}
				break;

			case EXCLAMATION:
				value.castBoolean();
				value.bool = !value.bool;
				break;
		}
	}

	public static void doIncrementOperation(ValueContainer value, SymbolType operation) throws ExecuteException {
		switch (value.type) {
			case DOUBLE:
				switch (operation) {
					case PLUS_PLUS:
						value.doubleNumber++;
						break;

					case MINUS_MINUS:
						value.doubleNumber--;
						break;
				}
				break;

			case LONG:
				switch (operation) {
					case PLUS_PLUS:
						value.longNumber++;
						break;

					case MINUS_MINUS:
						value.longNumber--;
						break;
				}
				break;

			case FLOAT:
				switch (operation) {
					case PLUS_PLUS:
						value.floatNumber++;
						break;

					case MINUS_MINUS:
						value.floatNumber--;
						break;
				}
				break;

			case INT:
				switch (operation) {
					case PLUS_PLUS:
						value.intNumber++;
						break;

					case MINUS_MINUS:
						value.intNumber--;
						break;
				}
				break;

			case SHORT:
				switch (operation) {
					case PLUS_PLUS:
						value.shortNumber++;
						break;

					case MINUS_MINUS:
						value.shortNumber--;
						break;
				}
				break;

			case BYTE:
				switch (operation) {
					case PLUS_PLUS:
						value.byteNumber++;
						break;

					case MINUS_MINUS:
						value.byteNumber--;
						break;
				}
				break;

			case CHAR:
				switch (operation) {
					case PLUS_PLUS:
						value.character++;
						break;

					case MINUS_MINUS:
						value.character--;
						break;
				}
				break;

			default:
				value.castInt();
				switch (operation) {
					case PLUS_PLUS:
						value.intNumber++;
						break;

					case MINUS_MINUS:
						value.intNumber--;
						break;
				}
				break;
		}
	}
}
