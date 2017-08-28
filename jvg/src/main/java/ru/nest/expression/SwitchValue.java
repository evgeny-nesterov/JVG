package ru.nest.expression;

public class SwitchValue extends Value {
	public SwitchValue(Value condition, Value onTrue, Value onFalse) {
		this.condition = condition;
		this.onTrue = onTrue;
		this.onFalse = onFalse;
	}

	private Value condition;

	private Value onTrue;

	private Value onFalse;

	@Override
	public double getValue(Trace trace) {
		return trace.getValue(condition) == OperationValue.TRUE ? trace.getValue(onTrue) : trace.getValue(onFalse);
	}
}
