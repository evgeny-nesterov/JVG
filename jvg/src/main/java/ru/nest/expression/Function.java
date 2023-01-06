package ru.nest.expression;

public interface Function {
	double getValue(Value[] arguments, Trace trace);

	int getArgumentsCount();
}
