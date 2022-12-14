package ru.nest.expression;

import java.util.HashMap;
import java.util.Map;

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

	private Map<Value, Double> values = new HashMap<>();

	public Map<Value, Double> getValues() {
		return values;
	}

	public void clear() {
		values.clear();
	}
}
