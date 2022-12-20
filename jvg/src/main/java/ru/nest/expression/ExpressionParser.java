package ru.nest.expression;

import java.util.ArrayList;
import java.util.List;

public class ExpressionParser {
	public ExpressionParser() {
		ctx = new ExpressionContext();
		t = new ExpressionTokenizer();
		tokens = new ArrayList<>();
	}

	private ExpressionContext ctx;

	public ExpressionContext getContext() {
		return ctx;
	}

	private ExpressionTokenizer t;

	private List<ExpressionToken> tokens;

	private int pos;

	private int len;

	private boolean beforeStart;

	private boolean afterEnd;

	private String cur;

	private int curType;

	private void next() {
		if (!afterEnd) {
			pos++;
			afterEnd = pos == len;
			if (!afterEnd) {
				ExpressionToken token = tokens.get(pos);
				cur = token.getToken();
				curType = token.getType();
			} else {
				cur = null;
				curType = -1;
			}
		} else {
			cur = null;
			curType = -1;
		}
	}

	private void prev() {
		if (!beforeStart) {
			pos--;
			beforeStart = pos == -1;
			if (!beforeStart) {
				ExpressionToken token = tokens.get(pos);
				cur = token.getToken();
				curType = token.getType();
			} else {
				cur = null;
				curType = -1;
			}
		} else {
			cur = null;
			curType = -1;
		}
	}

	public Value load(String id, String expression) throws Exception {
		tokens.clear();
		t.init(expression);
		while (t.hasNext()) {
			ExpressionToken token = t.next();
			if (token != null) {
				tokens.add(token);
			}
			// System.out.println(token);
		}
		pos = -1;
		len = tokens.size();
		beforeStart = true;
		afterEnd = false;
		next();

		Value value = getValue();

		if (pos != len) {
			throw new Exception("Parse exception: pos=" + pos + ", exp=" + expression);
		}

		if (id != null) {
			ctx.addVariable(id, value);
		}

		return value;
	}

	private Value getValue() throws Exception {
		ArrayList<Value> values = new ArrayList<>();
		ArrayList<Integer> operations = new ArrayList<>();

		while (!afterEnd) {
			// ========================================================================
			// === PREFIX
			// =============================================================
			// ========================================================================
			if ((values.size() == 0 && (curType == OperationValue.MINUS || curType == OperationValue.PLUS)) || curType == ExpressionToken.EXCLAMATION) {
				int prefixType = curType;
				next();

				Value value = getSimpleValue();
				if (value != null) {
					if (values.size() == operations.size() + 1) {
						throw new Exception("Operation is expected");
					}

					if (prefixType == OperationValue.MINUS) {
						values.add(new StandardFunctionValue(StandardFunctionValue.NEG, value));
					} else if (prefixType == ExpressionToken.EXCLAMATION) {
						values.add(new StandardFunctionValue(StandardFunctionValue.LOGNEG, value));
					} else if (prefixType == OperationValue.PLUS) {
						values.add(value);
					}
					continue;
				} else {
					throw new Exception("Value is expected");
				}
			}

			// ========================================================================
			// === Value
			// ==============================================================
			// ========================================================================
			Value value = getSimpleValue();
			if (value != null) {
				if (values.size() == operations.size() + 1) {
					throw new Exception("Operation is expected");
				}

				values.add(value);
				continue;
			}

			// ========================================================================
			// === Operation
			// ==========================================================
			// ========================================================================
			if (ExpressionToken.isOperation(curType)) {
				if (values.size() == 0) {
					throw new Exception("Operation at the start");
				}
				if (operations.size() == values.size()) {
					throw new Exception("Value is expected");
				}

				int operationType = curType;
				if (operations.size() > 0) {
					int curPriority = OperationValue.getPriority(operationType);
					int lastType, lastPriority;
					do {
						lastType = operations.get(operations.size() - 1);
						lastPriority = OperationValue.getPriority(lastType);
						if (curPriority <= lastPriority) {
							Value v2 = values.remove(values.size() - 1);
							Value v1 = values.remove(values.size() - 1);
							values.add(new OperationValue(lastType, v1, v2));
							operations.remove(operations.size() - 1);
						}
					} while (values.size() > 1 && curPriority <= lastPriority);
				}
				operations.add(operationType);

				next();
				continue;
			}

			// ========================================================================
			// === Switch
			// =============================================================
			// ========================================================================
			if (curType == ExpressionToken.QUESTION) {
				if (values.size() == 0) {
					throw new Exception("Value is expected");
				}
				Value valueCondition = values.remove(values.size() - 1);

				next();
				Value valueTrue = getValue();
				if (valueTrue == null) {
					throw new Exception("Value is expected");
				}

				if (curType != ExpressionToken.COLON) {
					throw new Exception("':' is expected");
				}
				next();

				Value valueFalse = getSimpleValue();
				if (valueFalse == null) {
					throw new Exception("Value is expected");
				}

				return new SwitchValue(valueCondition, valueTrue, valueFalse);
			}

			break;
		}

		doOperations(values, operations);
		if (values.size() > 0) {
			return values.get(0);
		} else {
			return null;
		}
	}

	private Value getSimpleValue() throws Exception {
		// ========================================================================
		// === Number
		// =============================================================
		// ========================================================================
		if (curType == ExpressionToken.NUMBER) {
			double value = Double.parseDouble(cur);
			next();
			return new NumberValue(value);
		}

		// ========================================================================
		// === Block
		// ==============================================================
		// ========================================================================
		if (curType == ExpressionToken.PARENTHESIS_OPEN) {
			next();
			Value value = getValue();
			if (value == null) {
				throw new Exception("Value is expected");
			}

			if (curType == ExpressionToken.PARENTHESIS_CLOSE) {
				next();
				return value;
			} else {
				throw new Exception("')' is expected");
			}
		}

		if (curType == ExpressionToken.WORD) {
			String word = cur;
			next();

			// ========================================================================
			// === Function
			// ===========================================================
			// ========================================================================
			if (curType == ExpressionToken.PARENTHESIS_OPEN) {
				next();
				boolean isStandard = StandardFunctionValue.isStandardFunction(word);
				boolean isManual = ctx.isFunction(word);

				if (!isStandard && !isManual) {
					throw new Exception("Function '" + word + "' doesn't exists");
				}

				ArrayList<Value> arguments = new ArrayList<>();
				getArguments(arguments);

				if (curType == ExpressionToken.PARENTHESIS_CLOSE) {
					FunctionValue function;
					if (isStandard) {
						int functionType = StandardFunctionValue.getFunctionType(word);
						function = new StandardFunctionValue(functionType, arguments);
					} else // manual
					{
						function = ctx.getFunction(word, arguments);
					}

					if (arguments.size() != function.getArgumentsCount()) {
						throw new Exception("Invalid number of arguments for function '" + word + "'");
					}

					next();
					return function;
				} else {
					throw new Exception("')' is expected");
				}
			}

			// ========================================================================
			// === Variable
			// ===========================================================
			// ========================================================================
			else {
				Value var = ctx.getVariable(word);
				if (var != null) {
					return var;
				} else {
					throw new Exception("Variable '" + word + "' is not found");
				}
			}
		}

		return null;
	}

	private boolean getArguments(List<Value> arguments) throws Exception {
		Value value = getValue();
		if (value == null) {
			return false;
		}
		arguments.add(value);

		while (curType == ExpressionToken.COMMA) {
			next();

			value = getValue();
			if (value == null) {
				throw new Exception("Value expected");
			}
			arguments.add(value);
		}

		return true;
	}

	private void doOperations(List<Value> values, List<Integer> operations) throws Exception {
		if (values.size() == 0) {
			throw new RuntimeException("Value is expected");
		}

		if (values.size() != operations.size() + 1) {
			throw new RuntimeException("Invalid sequence of values and operations");
		}

		while (values.size() > 1) {
			int type = operations.remove(operations.size() - 1);
			Value v2 = values.remove(values.size() - 1);
			Value v1 = values.remove(values.size() - 1);
			values.add(new OperationValue(type, v1, v2));
		}
	}

	public static void main(String[] args) {
		try {
			ExpressionParser p = new ExpressionParser();
			Value v1 = p.load("x", "sin(PI / 2)");
			Value v2 = p.load("y", "x + (1 + x) / 2 * (x - 2)");
			System.out.println("value = " + v2.getValue());

			// p.getContext().addFunction("func", new Function()
			// {
			// public double getValue(Value[] a)
			// {
			// return a[0].getValue() * a[1].getValue() - a[2].getValue() *
			// a[3].getValue();
			// }
			//
			//
			// public int getArgumentsCount()
			// {
			// return 4;
			// }
			// });
			//
			// Value value = p.load("x", "func(2,3,1,4)");
			// System.out.println(value.getValue());
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
