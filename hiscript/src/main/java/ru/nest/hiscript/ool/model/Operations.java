package ru.nest.hiscript.ool.model;

import ru.nest.hiscript.ool.model.operations.OperationAnd;
import ru.nest.hiscript.ool.model.operations.OperationArrayIndex;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftLeft;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftRight;
import ru.nest.hiscript.ool.model.operations.OperationBitwiseShiftRightCyclic;
import ru.nest.hiscript.ool.model.operations.OperationCast;
import ru.nest.hiscript.ool.model.operations.OperationDivide;
import ru.nest.hiscript.ool.model.operations.OperationEquals;
import ru.nest.hiscript.ool.model.operations.OperationEquate;
import ru.nest.hiscript.ool.model.operations.OperationEquateAnd;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftLeft;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftRight;
import ru.nest.hiscript.ool.model.operations.OperationEquateBitwiseShiftRightCyclic;
import ru.nest.hiscript.ool.model.operations.OperationEquateDivide;
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
import ru.nest.hiscript.ool.model.operations.OperationXOR;
import ru.nest.hiscript.tokenizer.SymbolType;

import static ru.nest.hiscript.ool.model.OperationType.*;

public class Operations {
	public static int getPriority(OperationType operation) {
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
			case DIVIDE:
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
			case INSTANCE_OF:
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

	public static int getOperandsCount(OperationType operation) {
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
		}
		return OPERANDS_BINARY;
	}

	public static boolean isOperation(OperationType operation) {
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
			case DIVIDE:
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
			case INSTANCE_OF:
			case EQUALS:
			case NOT_EQUALS:
			case AND:
			case XOR:
			case OR:
			case LOGICAL_AND:
			case LOGICAL_AND_CHECK:
			case LOGICAL_OR:
			case LOGICAL_OR_CHECK:
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
			case EQUATE_OR:
			case EQUATE_XOR:
				return true;
		}
		return false;
	}

	public static OperationType mapSymbolToOperation(SymbolType symbol) {
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
			case MASSIVE:
				return ARRAY_INDEX;

			case POINT:
				return INVOCATION;

			case MULTIPLY:
				return MULTIPLY;

			case DIVIDE:
				return DIVIDE;

			case PERCENT:
				return PERCENT;

			case PLUS:
				return PLUS;

			case MINUS:
				return MINUS;

			case BITWISE_SHIFT_LEFT:
				return BITWISE_SHIFT_LEFT;

			case BITWISE_SHIFT_RIGHT:
				return BITWISE_SHIFT_RIGHT;

			case BITWISE_SHIFT_RIGHT_CYCLIC:
				return BITWISE_SHIFT_RIGHT_CYCLIC;

			case LOWER:
				return LOWER;

			case LOWER_OR_EQUAL:
				return LOWER_OR_EQUALS;

			case GREATER:
				return GREATER;

			case GREATER_OR_EQUAL:
				return GREATER_OR_EQUALS;

			case EQUALS:
				return EQUALS;

			case NOT_EQUALS:
				return NOT_EQUALS;

			case BITWISE_AND:
				return AND;

			case BITWISE_XOR:
				return XOR;

			case BITWISE_OR:
				return OR;

			case LOGICAL_AND:
				return LOGICAL_AND;

			case LOGICAL_OR:
				return LOGICAL_OR;

			case EQUATE:
				return EQUATE;

			case EQUATE_PLUS:
				return EQUATE_PLUS;

			case EQUATE_MINUS:
				return EQUATE_MINUS;

			case EQUATE_MULTIPLY:
				return EQUATE_MULTIPLY;

			case EQUATE_DIVIDE:
				return EQUATE_DIVIDE;

			case EQUATE_PERCENT:
				return EQUATE_PERCENT;

			case EQUATE_BITWISE_SHIFT_LEFT:
				return EQUATE_BITWISE_SHIFT_LEFT;

			case EQUATE_BITWISE_SHIFT_RIGHT:
				return EQUATE_BITWISE_SHIFT_RIGHT;

			case EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC:
				return EQUATE_BITWISE_SHIFT_RIGHT_CYCLIC;

			case EQUATE_BITWISE_AND:
				return EQUATE_AND;

			case EQUATE_BITWISE_XOR:
				return EQUATE_XOR;

			case EQUATE_BITWISE_OR:
				return EQUATE_OR;
		}
		return null;
	}

	public static String getName(OperationType operation) {
		switch (operation) {
			case ARRAY_INDEX:
				return "[]";

			case INVOCATION:
				return ".";

			case POST_INCREMENT:
			case PREFIX_INCREMENT:
				return "++";

			case POST_DECREMENT:
			case PREFIX_DECREMENT:
				return "--";

			case PLUS:
			case PREFIX_PLUS:
				return "+";

			case MINUS:
			case PREFIX_MINUS:
				return "-";

			case PREFIX_EXCLAMATION:
				return "!";

			case PREFIX_BITWISE_REVERSE:
				return "~";

			case CAST:
				return "(cast)";

			case MULTIPLY:
				return "*";

			case DIVIDE:
				return "/";

			case PERCENT:
				return "%";

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

			case INSTANCE_OF:
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

			case LOGICAL_AND_CHECK:
				return "?&&";

			case LOGICAL_OR:
				return "||";

			case LOGICAL_OR_CHECK:
				return "?||";

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
				return "|=";
		}
		return "<No such operation: " + operation + ">";
	}

	public static HiOperation getOperation(OperationType operation) {
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

			case DIVIDE:
				return OperationDivide.getInstance();

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

			case INSTANCE_OF:
				return OperationInstanceOf.getInstance();

			case LOGICAL_AND:
				return OperationLogicalAnd.getInstance();

			case LOGICAL_AND_CHECK:
				return OperationLogicalAndCheck.getInstance();

			case LOGICAL_OR:
				return OperationLogicalOR.getInstance();

			case LOGICAL_OR_CHECK:
				return OperationLogicalOrCheck.getInstance();

			case EQUATE:
				return OperationEquate.getInstance();

			case EQUATE_PLUS:
				return OperationEquatePlus.getInstance();

			case EQUATE_MINUS:
				return OperationEquateMinus.getInstance();

			case EQUATE_MULTIPLY:
				return OperationEquateMultiply.getInstance();

			case EQUATE_DIVIDE:
				return OperationEquateDivide.getInstance();

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
		}
		return null;
	}
}
