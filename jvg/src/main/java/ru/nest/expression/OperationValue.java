package ru.nest.expression;

public class OperationValue extends Value {
	public final static int BITS = 5;

	public final static int PLUS = 1;

	public final static int MINUS = 2;

	public final static int MULT = 3;

	public final static int DIV = 4;

	public final static int GREATER = 5;

	public final static int LESS = 6;

	public final static int EQUAL = 7;

	public final static int GREATER_OR_EQUAL = 8;

	public final static int LESS_OR_EQUAL = 9;

	public final static int NOT_EQUAL = 10;

	public final static int RESIDUE = 11;

	public final static int OR = 12;

	public final static int AND = 13;

	public final static int BINARY_SHIFT_RIGHT = 14;

	public final static int BINARY_SHIFT_LEFT = 15;

	public final static int BINARY_AND = 16;

	public final static int BINARY_OR = 17;

	public final static int BINARY_EXCLUSIVE_OR = 18;

	public final static int TRUE = 1;

	public final static int FALSE = -1;

	public OperationValue(int operationType, Value v1, Value v2) {
		this.operationType = operationType;
		this.v1 = v1;
		this.v2 = v2;
	}

	private int operationType;

	private Value v1;

	private Value v2;

	@Override
	public double getValue(Trace trace) {
		double V1 = trace.getValue(v1);
		double V2 = trace.getValue(v2);

		switch (operationType) {
			case PLUS:
				return V1 + V2;

			case MINUS:
				return V1 - V2;

			case MULT:
				return V1 * V2;

			case DIV:
				double znam = V2;
				if (znam != 0) {
					return V1 / znam;
				} else {
					return Double.NaN;
				}

			case GREATER:
				return V1 > V2 ? TRUE : FALSE;

			case LESS:
				return V1 < V2 ? TRUE : FALSE;

			case EQUAL:
				return V1 == V2 ? TRUE : FALSE;

			case NOT_EQUAL:
				return V1 != V2 ? TRUE : FALSE;

			case GREATER_OR_EQUAL:
				return V1 >= V2 ? TRUE : FALSE;

			case LESS_OR_EQUAL:
				return V1 <= V2 ? TRUE : FALSE;

			case RESIDUE:
				return V1 % V2;

			case OR:
				return (V1 == TRUE || V2 == TRUE) ? TRUE : FALSE;

			case AND:
				return (V1 == TRUE && V2 == TRUE) ? TRUE : FALSE;

			case BINARY_SHIFT_RIGHT:
				return (long) V1 >> (long) V2;

			case BINARY_SHIFT_LEFT:
				return (long) V1 << (long) V2;

			case BINARY_AND:
				return (long) V1 & (long) V2;

			case BINARY_OR:
				return (long) V1 | (long) V2;

			case BINARY_EXCLUSIVE_OR:
				return (long) V1 ^ (long) V2;
		}

		return 0;
	}

	public static int getPriority(int operationType) {
		switch (operationType) {
			case OR:
				return 12;

			case AND:
				return 11;

			case BINARY_AND:
			case BINARY_OR:
			case BINARY_EXCLUSIVE_OR:
			case BINARY_SHIFT_RIGHT:
			case BINARY_SHIFT_LEFT:
			case GREATER:
			case LESS:
			case EQUAL:
			case GREATER_OR_EQUAL:
			case LESS_OR_EQUAL:
				return 10;

			case PLUS:
			case MINUS:
				return 1;

			case RESIDUE:
				return 2;

			case MULT:
				return 3;

			case DIV:
				return 4;
		}

		return 0;
	}
}
