package ru.nest.expression;

import java.util.HashMap;

public class Trace {
	public double getValue(Value v) {
		if (values.containsKey(v)) {
			return values.get(v);
		} else {
			double value = v.getValue(this);
			values.put(v, value);
			return value;
		}
	}

	private HashMap<Value, Double> values = new HashMap<>();

	public HashMap<Value, Double> getValues() {
		return values;
	}

	public void clear() {
		values.clear();
	}
}
