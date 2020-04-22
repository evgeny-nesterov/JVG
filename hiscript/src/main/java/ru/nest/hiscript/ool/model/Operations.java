package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.operations.OperationAnd;
import ru.nest.hiscript.ool.model.operations.OperationArrayIndex;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftLeft;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftRight;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftRightCyclic;
import ru.nest.hiscript.ool.model.operations.OperationCast;
import ru.nest.hiscript.ool.model.operations.OperationDevide;
import ru.nest.hiscript.ool.model.operations.OperationEquals;
import ru.nest.hiscript.ool.model.operations.OperationEquate;
import ru.nest.hiscript.ool.model.operations.OperationEquateAnd;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftLeft;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftRight;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftRightCyclic;
import ru.nest.hiscript.ool.model.operations.OperationEquateDevide;
import ru.nest.hiscript.ool.model.operations.OperationEquateMinus;
import ru.nest.hiscript.ool.model.operations.OperationEquateMultiply;
import ru.nest.hiscript.ool.model.operations.OperationEquateOR;
import ru.nest.hiscript.ool.model.operations.OperationEquatePercent;
import ru.nest.hiscript.ool.model.operations.OperationEquatePlus;
import ru.nest.hiscript.ool.model.operations.OperationEquateXOR;
import ru.nest.hiscript.ool.model.operations.OperationGreater;
import ru.nest.hiscript.ool.model.operations.OperationGreaterOrEquals;
import ru.nest.hiscript.ool.model.operations.OperationInstanceOf;
import ru.nest.hiscript.ool.model.operations.OperationInvocation;
import ru.nest.hiscript.ool.model.operations.OperationLogicalAnd;
import ru.nest.hiscript.ool.model.operations.OperationLogicalAndCheck;
import ru.nest.hiscript.ool.model.operations.OperationLogicalOR;
import ru.nest.hiscript.ool.model.operations.OperationLogicalOrCheck;
import ru.nest.hiscript.ool.model.operations.OperationLower;
import ru.nest.hiscript.ool.model.operations.OperationLowerOrEquals;
import ru.nest.hiscript.ool.model.operations.OperationMinus;
import ru.nest.hiscript.ool.model.operations.OperationMultiply;
import ru.nest.hiscript.ool.model.operations.OperationNotEquals;
import ru.nest.hiscript.ool.model.operations.OperationOR;
import ru.nest.hiscript.ool.model.operations.OperationPercent;
import ru.nest.hiscript.ool.model.operations.OperationPlus;
import ru.nest.hiscript.ool.model.operations.OperationPostfixDecrement;
import ru.nest.hiscript.ool.model.operations.OperationPostfixIncrement;
import ru.nest.hiscript.ool.model.operations.OperationPrefixBitwiseReverse;
import ru.nest.hiscript.ool.model.operations.OperationPrefixDecrement;
import ru.nest.hiscript.ool.model.operations.OperationPrefixExclamation;
import ru.nest.hiscript.ool.model.operations.OperationPrefixIncrement;
import ru.nest.hiscript.ool.model.operations.OperationPrefixMinus;
import ru.nest.hiscript.ool.model.operations.OperationPrefixPlus;
import ru.nest.hiscript.ool.model.operations.OperationTriger;
import ru.nest.hiscript.ool.model.operations.OperationXOR;
import ru.nest.hiscript.ool.model.operations.SkipOperation;
import ru.nest.hiscript.tokenizer.Symbols;

public class Operations implements OperationsIF, PrimitiveTypes {
	public static int getPriority(int operation) {
		switch (operation) {
			case INVOCATION:
			case ARRAY_INDEX:
			case POST_INCREMENT:
			case POST_DECREMENT:
				return 0;

			case PREFIX_INCREMENT:
			case PREFIX_DECREMENT:
			case PREFIX_PLUS:
			case PREFIX_MINUS:
			case PREFIX_EXCLAMATION:
			case PREFIX_BITWISE_REVERSE:
				return 10;

			case CAST:
				return 20;

			case MULTIPLY:
			case DEVIDE:
			case PERCENT:
				return 30;

			case PLUS:
			case MINUS:
				return 40;

			case BITWISE_SHIFT_LEFT:
			case BITWISE_SHIFT_RIGHT:
			case BITWISE_SHIFT_RIGHT_CYCLIC:
				return 50;

			case LOWER:
			case LOWER_OR_EQUALS:
			case GREATER:
			case GREATER_OR_EQUALS:
			case INSTANCEOF:
				return 60;

			case EQUALS:
			case NOT_EQUALS:
				return 70;

			case AND:
				return 80;

			case XOR:
				return 90;

			case OR:
				return 100;

			case LOGICAL_AND_CHECK:
				return 110;

			case LOGICAL_AND:
				return 111;

			case LOGICAL_OR_CHECK:
				return 112;

			case LOGICAL_OR:
				return 113;

			case SKIP:
				return 129;
			case TRIGER:
				return 130;

			case EQUATE:
			case EQUATE_PLUS:
			case EQUATE_MINUS:
			case EQUATE_MULTIPLY:
			case EQUATE_DIVIDE:
			case EQUATE_PERCENT:
			case EQUATE_BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_AND:
			case EQUATE_XOR:
			case EQUATE_OR:
				return 1000;
		}

		return 9999;
	}

	public final static int OPERANDS_UNARY = 1;

	public final static int OPERANDS_BINARY = 2;

	public final static int OPERANDS_TRINARY = 3;

	public static int getOparandsCount(int operation) {
		switch (operation) {
			case POST_INCREMENT:
			case POST_DECREMENT:
			case PREFIX_INCREMENT:
			case PREFIX_DECREMENT:
			case PREFIX_PLUS:
			case PREFIX_MINUS:
			case PREFIX_EXCLAMATION:
			case PREFIX_BITWISE_REVERSE:
				return OPERANDS_UNARY;

			case TRIGER:
				return OPERANDS_TRINARY;
		}

		return OPERANDS_BINARY;
	}

	public static boolean isOperation(int operation) {
		switch (operation) {
			case ARRAY_INDEX:
			case INVOCATION:
			case POST_INCREMENT:
			case POST_DECREMENT:
			case PREFIX_INCREMENT:
			case PREFIX_DECREMENT:
			case PREFIX_PLUS:
			case PREFIX_MINUS:
			case PREFIX_EXCLAMATION:
			case PREFIX_BITWISE_REVERSE:
			case CAST:
			case MULTIPLY:
			case DEVIDE:
			case PERCENT:
			case PLUS:
			case MINUS:
			case BITWISE_SHIFT_LEFT:
			case BITWISE_SHIFT_RIGHT:
			case BITWISE_SHIFT_RIGHT_CYCLIC:
			case LOWER:
			case LOWER_OR_EQUALS:
			case GREATER:
			case GREATER_OR_EQUALS:
			case INSTANCEOF:
			case EQUALS:
			case NOT_EQUALS:
			case AND:
			case XOR:
			case OR:
			case LOGICAL_AND:
			case LOGICAL_OR:
			case SKIP:
			case TRIGER:
			case EQUATE:
			case EQUATE_PLUS:
			case EQUATE_MINUS:
			case EQUATE_MULTIPLY:
			case EQUATE_DIVIDE:
			case EQUATE_PERCENT:
			case EQUATE_BITWISE_SHIFT_LEFT:
			case EQUATE_BITWISE_SHIFT_RIGHT:
			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
			case EQUATE_AND:
			case EQUATE_XOR:
			case EQUATE_OR:
			case LOGICAL_AND_CHECK:
			case LOGICAL_OR_CHECK:
				return true;
		}

		return false;
	}

	public static int mapSymbolToOperation(int symbol) {
		// POST_INCREMENT
		// POST_DECREMENT
		// PREFIX_INCREMENT
		// PREFIX_DECREMENT
		// PREFIX_PLUS
		// PREFIX_MINUS
		// PREFIX_EXCLAMATION
		// PREFIX_BITWISE_REVERSE
		// CAST

		switch (symbol) {
			case Symbols.MASSIVE:
				return ARRAY_INDEX;

			case Symbols.POINT:
				return INVOCATION;

			case Symbols.MULTIPLY:
				return MULTIPLY;

			case Symbols.DEVIDE:
				return DEVIDE;

			case Symbols.PERCENT:
				return PERCENT;

			case Symbols.PLUS:
				return PLUS;

			case Symbols.MINUS:
				return MINUS;

			case Symbols.BITWISE_SHIFT_LEFT:
				return BITWISE_SHIFT_LEFT;

			case Symbols.BITWISE_SHIFT_RIGHT:
				return BITWISE_SHIFT_RIGHT;

			case Symbols.BITWISE_SHIFT_RIGHT_CYCLIC:
				return BITWISE_SHIFT_RIGHT_CYCLIC;

			case Symbols.LOWER:
				return LOWER;

			case Symbols.LOWER_OR_EQUAL:
				return LOWER_OR_EQUALS;

			case Symbols.GREATER:
				return GREATER;

			case Symbols.GREATER_OR_EQUAL:
				return GREATER_OR_EQUALS;

			case Symbols.EQUALS:
				return EQUALS;

			case Symbols.NOT_EQUALS:
				return NOT_EQUALS;

			case Symbols.BITWISE_AND:
				return AND;

			case Symbols.BITWISE_XOR:
				return XOR;

			case Symbols.BITWISE_OR:
				return OR;

			case Symbols.LOGICAL_AND:
				return LOGICAL_AND;

			case Symbols.LOGICAL_OR:
				return LOGICAL_OR;

			case Symbols.EQUATE:
				return EQUATE;

			case Symbols.EQUATE_PLUS:
				return EQUATE_PLUS;

			case Symbols.EQUATE_MINUS:
				return EQUATE_MINUS;

			case Symbols.EQUATE_MULTIPLY:
				return EQUATE_MULTIPLY;

			case Symbols.EQUATE_DEVIDE:
				return EQUATE_DIVIDE;

			case Symbols.EQUATE_PERCENT:
				return EQUATE_PERCENT;

			case Symbols.EQUATE_BITWISE_SHIFT_LEFT:
				return EQUATE_BITWISE_SHIFT_LEFT;

			case Symbols.EQUATE_BITWISE_SHIFT_RIGHT:
				return EQUATE_BITWISE_SHIFT_RIGHT;

			case Symbols.EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				return EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC;

			case Symbols.EQUATE_BITWISE_AND:
				return EQUATE_AND;

			case Symbols.EQUATE_BITWISE_XOR:
				return EQUATE_XOR;

			case Symbols.EQUATE_BITWISE_OR:
				return EQUATE_OR;

			case Symbols.QUESTION:
				return SKIP;

			case Symbols.COLON:
				return TRIGER;
		}

		return -1;
	}

	public static String getName(int operation) {
		switch (operation) {
			case ARRAY_INDEX:
				return "[]";

			case INVOCATION:
				return ".";

			case POST_INCREMENT:
				return "++";

			case POST_DECREMENT:
				return "--";

			case PREFIX_INCREMENT:
				return "++";

			case PREFIX_DECREMENT:
				return "--";

			case PREFIX_PLUS:
				return "+";

			case PREFIX_MINUS:
				return "-";

			case PREFIX_EXCLAMATION:
				return "!";

			case PREFIX_BITWISE_REVERSE:
				return "~";

			case CAST:
				return "(<type>)";

			case MULTIPLY:
				return "*";

			case DEVIDE:
				return "/";

			case PERCENT:
				return "%";

			case PLUS:
				return "+";

			case MINUS:
				return "-";

			case BITWISE_SHIFT_LEFT:
				return "<<";

			case BITWISE_SHIFT_RIGHT:
				return ">>";

			case BITWISE_SHIFT_RIGHT_CYCLIC:
				return ">>>";

			case LOWER:
				return "<";

			case LOWER_OR_EQUALS:
				return "<=";

			case GREATER:
				return ">";

			case GREATER_OR_EQUALS:
				return ">=";

			case INSTANCEOF:
				return "instanceof";

			case EQUALS:
				return "==";

			case NOT_EQUALS:
				return "!=";

			case AND:
				return "&";

			case XOR:
				return "^";

			case OR:
				return "|";

			case LOGICAL_AND:
				return "&&";

			case LOGICAL_OR:
				return "||";

			case TRIGER:
				return "?:";

			case EQUATE:
				return "=";

			case EQUATE_PLUS:
				return "+=";

			case EQUATE_MINUS:
				return "-=";

			case EQUATE_MULTIPLY:
				return "*=";

			case EQUATE_DIVIDE:
				return "/=";

			case EQUATE_PERCENT:
				return "%=";

			case EQUATE_BITWISE_SHIFT_LEFT:
				return "<<=";

			case EQUATE_BITWISE_SHIFT_RIGHT:
				return ">>=";

			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				return ">>>=";

			case EQUATE_AND:
				return "&=";

			case EQUATE_XOR:
				return "^=";

			case EQUATE_OR:
				return "=|";

			case SKIP:
				return "->";

			case LOGICAL_AND_CHECK:
				return "?&&";

			case LOGICAL_OR_CHECK:
				return "?||";
		}

		return "<No such operation: " + operation + ">";
	}

	public static Operation getOperation(int operation) {
		switch (operation) {
			// Unary operations
			case PREFIX_INCREMENT:
				return OperationPrefixIncrement.getInstance();

			case PREFIX_DECREMENT:
				return OperationPrefixDecrement.getInstance();

			case PREFIX_EXCLAMATION:
				return OperationPrefixExclamation.getInstance();

			case PREFIX_BITWISE_REVERSE:
				return OperationPrefixBitwiseReverse.getInstance();

			case PREFIX_PLUS:
				return OperationPrefixPlus.getInstance();

			case PREFIX_MINUS:
				return OperationPrefixMinus.getInstance();

			case POST_INCREMENT:
				return OperationPostfixIncrement.getInstance();

			case POST_DECREMENT:
				return OperationPostfixDecrement.getInstance();

			// Binary operations
			case ARRAY_INDEX:
				return OperationArrayIndex.getInstance();

			case INVOCATION:
				return OperationInvocation.getInstance();

			case CAST:
				return OperationCast.getInstance();

			case MULTIPLY:
				return OperationMultiply.getInstance();

			case DEVIDE:
				return OperationDevide.getInstance();

			case PERCENT:
				return OperationPercent.getInstance();

			case PLUS:
				return OperationPlus.getInstance();

			case MINUS:
				return OperationMinus.getInstance();

			case BITWISE_SHIFT_LEFT:
				return OperationBitwiseShiftLeft.getInstance();

			case BITWISE_SHIFT_RIGHT:
				return OperationBitwiseShiftRight.getInstance();

			case BITWISE_SHIFT_RIGHT_CYCLIC:
				return OperationBitwiseShiftRightCyclic.getInstance();

			case AND:
				return OperationAnd.getInstance();

			case XOR:
				return OperationXOR.getInstance();

			case OR:
				return OperationOR.getInstance();

			case LOWER:
				return OperationLower.getInstance();

			case LOWER_OR_EQUALS:
				return OperationLowerOrEquals.getInstance();

			case GREATER:
				return OperationGreater.getInstance();

			case GREATER_OR_EQUALS:
				return OperationGreaterOrEquals.getInstance();

			case EQUALS:
				return OperationEquals.getInstance();

			case NOT_EQUALS:
				return OperationNotEquals.getInstance();

			case INSTANCEOF:
				return OperationInstanceOf.getInstance();

			case LOGICAL_AND:
				return OperationLogicalAnd.getInstance();

			case LOGICAL_OR:
				return OperationLogicalOR.getInstance();

			case EQUATE:
				return OperationEquate.getInstance();

			case EQUATE_PLUS:
				return OperationEquatePlus.getInstance();

			case EQUATE_MINUS:
				return OperationEquateMinus.getInstance();

			case EQUATE_MULTIPLY:
				return OperationEquateMultiply.getInstance();

			case EQUATE_DIVIDE:
				return OperationEquateDevide.getInstance();

			case EQUATE_PERCENT:
				return OperationEquatePercent.getInstance();

			case EQUATE_BITWISE_SHIFT_LEFT:
				return OperationEquateBitwiseShiftLeft.getInstance();

			case EQUATE_BITWISE_SHIFT_RIGHT:
				return OperationEquateBitwiseShiftRight.getInstance();

			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				return OperationEquateBitwiseShiftRightCyclic.getInstance();

			case EQUATE_AND:
				return OperationEquateAnd.getInstance();

			case EQUATE_XOR:
				return OperationEquateXOR.getInstance();

			case EQUATE_OR:
				return OperationEquateOR.getInstance();

			case TRIGER:
				return OperationTriger.getInstance();

			case SKIP:
				return SkipOperation.getInstance();

			case LOGICAL_AND_CHECK:
				return OperationLogicalAndCheck.getInstance();

			case LOGICAL_OR_CHECK:
				return OperationLogicalOrCheck.getInstance();
		}

		return null;
	}
}
