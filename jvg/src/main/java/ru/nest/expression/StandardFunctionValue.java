package ru.nest.expression;

import java.util.ArrayList;
import java.util.HashMap;

public class StandardFunctionValue extends FunctionValue {
	public final static int SIN = 0;

	public final static int COS = 1;

	public final static int TAN = 2;

	public final static int ASIN = 3;

	public final static int ACOS = 4;

	public final static int ATAN = 5;

	public final static int SQRT = 6;

	public final static int CBRT = 7;

	public final static int POW = 8;

	public final static int ABS = 9;

	public final static int CEIL = 10;

	public final static int FLOOR = 11;

	public final static int EXP = 12;

	public final static int LOG = 13;

	public final static int LN = 14;

	public final static int MAX = 15;

	public final static int MIN = 16;

	public final static int RINT = 17;

	public final static int SIGNUM = 18;

	public final static int RANDOM = 19;

	public final static int NEG = 20;

	public final static int LOGNEG = 21;

	public final static int SIGN = 22;

	private static HashMap<String, Integer> standardFunctions = new HashMap<>();
	static {
		standardFunctions.put("sin", SIN);
		standardFunctions.put("cos", COS);
		standardFunctions.put("tan", TAN);
		standardFunctions.put("asin", ASIN);
		standardFunctions.put("acos", ACOS);
		standardFunctions.put("atan", ATAN);
		standardFunctions.put("sqrt", SQRT);
		standardFunctions.put("cbrt", CBRT);
		standardFunctions.put("pow", POW);
		standardFunctions.put("abs", ABS);
		standardFunctions.put("ceil", CEIL);
		standardFunctions.put("floor", FLOOR);
		standardFunctions.put("exp", EXP);
		standardFunctions.put("log", LOG);
		standardFunctions.put("ln", LN);
		standardFunctions.put("max", MAX);
		standardFunctions.put("min", MIN);
		standardFunctions.put("rint", RINT);
		standardFunctions.put("signum", SIGNUM);
		standardFunctions.put("rnd", RANDOM);
		standardFunctions.put("sign", SIGN);
	}

	public static boolean isStandardFunction(String func) {
		return standardFunctions.containsKey(func);
	}

	public static int getFunctionType(String func) {
		return standardFunctions.get(func);
	}

	public StandardFunctionValue(int func, Value[] arguments) {
		super(arguments);
		this.type = func;
	}

	public StandardFunctionValue(int func, Value argument) {
		super(argument);
		this.type = func;
	}

	public StandardFunctionValue(int func, ArrayList<Value> arguments) {
		super(arguments);
		this.type = func;
	}

	private int type;

	public int getType() {
		return type;
	}

	@Override
	public double getValue(Trace trace) {
		switch (type) {
			case SIN:
				return Math.sin(trace.getValue(arguments[0]));

			case COS:
				return Math.cos(trace.getValue(arguments[0]));

			case TAN:
				return Math.tan(trace.getValue(arguments[0]));

			case ASIN:
				return Math.asin(trace.getValue(arguments[0]));

			case ACOS:
				return Math.acos(trace.getValue(arguments[0]));

			case ATAN:
				return Math.atan(trace.getValue(arguments[0]));

			case SQRT:
				return Math.sqrt(trace.getValue(arguments[0]));

			case CBRT:
				return Math.cbrt(trace.getValue(arguments[0]));

			case POW:
				return Math.pow(trace.getValue(arguments[0]), trace.getValue(arguments[1]));

			case ABS:
				return Math.abs(trace.getValue(arguments[0]));

			case CEIL:
				return Math.ceil(trace.getValue(arguments[0]));

			case FLOOR:
				return Math.floor(trace.getValue(arguments[0]));

			case EXP:
				return Math.exp(trace.getValue(arguments[0]));

			case LOG:
				return Math.log10(trace.getValue(arguments[0]));

			case LN:
				return Math.log(trace.getValue(arguments[0]));

			case MAX:
				return Math.max(trace.getValue(arguments[0]), trace.getValue(arguments[1]));

			case MIN:
				return Math.min(trace.getValue(arguments[0]), trace.getValue(arguments[1]));

			case RINT:
				return Math.rint(trace.getValue(arguments[0]));

			case SIGNUM:
				return Math.signum(trace.getValue(arguments[0]));

			case RANDOM:
				return Math.random();

			case NEG:
				return -trace.getValue(arguments[0]);

			case LOGNEG:
				return trace.getValue(arguments[0]) == OperationValue.TRUE ? OperationValue.FALSE : OperationValue.TRUE;

			case SIGN:
				double value = trace.getValue(arguments[0]);
				return (value > 0) ? 1 : (value < 0 ? -1 : 0);
		}
		return 0;
	}

	@Override
	public int getArgumentsCount() {
		return getArgumentsCount(type);
	}

	public static int getArgumentsCount(int func) {
		switch (func) {
			case RANDOM:
				return 0;

			case SIN:
			case COS:
			case TAN:
			case ASIN:
			case ACOS:
			case ATAN:
			case SQRT:
			case CBRT:
			case ABS:
			case CEIL:
			case FLOOR:
			case EXP:
			case LOG:
			case LN:
			case RINT:
			case SIGNUM:
			case NEG:
			case LOGNEG:
			case SIGN:
				return 1;

			case POW:
			case MIN:
			case MAX:
				return 2;
		}

		return 0;
	}
}
