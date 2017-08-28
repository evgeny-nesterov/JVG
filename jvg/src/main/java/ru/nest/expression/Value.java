package ru.nest.expression;

public abstract class Value {
	private Trace trace;

	public Trace getTrace() {
		return trace;
	}

	public double getValue() {
		if (trace == null) {
			trace = new Trace();
		} else {
			trace.clear();
		}

		return trace.getValue(this);
	}

	public abstract double getValue(Trace trace);
}
