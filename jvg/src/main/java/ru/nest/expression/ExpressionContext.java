package ru.nest.expression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionContext {
	public ExpressionContext() {
		variables.put("PI", new NumberValue(Math.PI));
		variables.put("E", new NumberValue(Math.E));
	}

	private Map<String, Value> variables = new HashMap<>();

	public void addVariable(String id, Value var) {
		variables.put(id, var);
	}

	public Value getVariable(String id) {
		return variables.get(id);
	}

	private Map<String, Function> functions = new HashMap<>();

	public boolean isFunction(String id) {
		return functions.containsKey(id);
	}

	public FunctionValue getFunction(String id, Value[] arguments) {
		Function function = functions.get(id);
		if (function != null) {
			return new DefaultFanctionValue(function, arguments);
		} else {
			return null;
		}
	}

	public FunctionValue getFunction(String id, List<Value> arguments) {
		Function function = functions.get(id);
		if (function != null) {
			return new DefaultFanctionValue(function, arguments);
		} else {
			return null;
		}
	}

	public FunctionValue getFunction(String id, Value argument) {
		Function function = functions.get(id);
		if (function != null) {
			return new DefaultFanctionValue(function, argument);
		} else {
			return null;
		}
	}

	public void addFunction(String id, Function function) {
		functions.put(id, function);
	}

	public void addFunctions(Map<String, Function> functions) {
		if (functions != null) {
			this.functions.putAll(functions);
		}
	}
}
