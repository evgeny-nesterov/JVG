package ru.nest.expression;

public class NumberValue extends Value {
	public NumberValue(double value) {
		this.value = value;
	}

	private double value;

	@Override
	public double getValue(Trace trace) {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}
}
