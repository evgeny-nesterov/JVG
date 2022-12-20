package ru.nest.expression;

import java.util.List;

public abstract class FunctionValue extends Value {
	public FunctionValue(Value[] arguments) {
		this.arguments = arguments;
	}

	public FunctionValue(Value argument) {
		this.arguments = new Value[] { argument };
	}

	public FunctionValue(List<Value> arguments) {
		if (arguments != null) {
			this.arguments = new Value[arguments.size()];
			for (int i = 0; i < arguments.size(); i++) {
				this.arguments[i] = arguments.get(i);
			}
		}
	}

	protected Value[] arguments;

	public Value[] getArguments() {
		return arguments;
	}

	public abstract int getArgumentsCount();
}
