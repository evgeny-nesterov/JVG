package ru.nest.expression;

import java.util.List;

public class DefaultFanctionValue extends FunctionValue {
	public DefaultFanctionValue(Function function, Value[] arguments) {
		super(arguments);
		this.function = function;
	}

	public DefaultFanctionValue(Function function, Value argument) {
		super(argument);
		this.function = function;
	}

	public DefaultFanctionValue(Function function, List<Value> arguments) {
		super(arguments);
		this.function = function;
	}

	private Function function;

	public Function getFunction() {
		return function;
	}

	@Override
	public double getValue(Trace trace) {
		return function.getValue(arguments, trace);
	}

	@Override
	public int getArgumentsCount() {
		return function.getArgumentsCount();
	}
}
