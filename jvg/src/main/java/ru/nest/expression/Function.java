package ru.nest.expression;

public interface Function {
	public double getValue(Value[] arguments, Trace trace);

	public int getArgumentsCount();
}
