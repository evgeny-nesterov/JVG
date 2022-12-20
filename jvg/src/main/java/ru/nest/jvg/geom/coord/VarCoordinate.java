package ru.nest.jvg.geom.coord;

import java.util.List;

import ru.nest.expression.NumberValue;
import ru.nest.expression.Trace;
import ru.nest.expression.Value;

public class VarCoordinate extends AbstractCoordinate {
	public VarCoordinate(Value get) {
		this.get = get;
	}

	public VarCoordinate(Value get, NumberValue[] set, Value[] function, NumberValue argument, boolean transformable) {
		this.get = get;
		this.set = set;
		this.function = function;
		this.argument = argument;
		this.transformable = transformable;

		setManyArguments = this.set != null && this.function != null && this.argument != null;
		setNumber = this.get instanceof NumberValue;
	}

	public VarCoordinate(Value get, List<NumberValue> set, List<Value> function, NumberValue argument, boolean transformable) {
		this.get = get;
		if (set != null && function != null && argument != null) {
			this.set = new NumberValue[set.size()];
			this.function = new Value[function.size()];
			int len = Math.min(set.size(), function.size());
			for (int i = 0; i < len; i++) {
				this.set[i] = set.get(i);
				this.function[i] = function.get(i);
			}

			this.argument = argument;
		}
		this.transformable = transformable;

		setManyArguments = this.set != null && this.function != null && this.argument != null;
		setNumber = this.get instanceof NumberValue;
	}

	private Value get;

	private NumberValue[] set;

	private Value[] function;

	private NumberValue argument;

	private boolean transformable;

	private boolean setManyArguments, setNumber;

	@Override
	public double getCoord() {
		return get.getValue();
	}

	private Trace trace;

	@Override
	public void setCoord(double coord) {
		if (setManyArguments) {
			if (trace == null) {
				trace = new Trace();
			} else {
				trace.clear();
			}

			argument.setValue(coord);
			int len = Math.min(set.length, function.length);
			for (int i = 0; i < len; i++) {
				double value = function[i].getValue(trace);
				set[i].setValue(value);
			}
		} else if (setNumber) {
			NumberValue set = (NumberValue) get;
			set.setValue(coord);
		}
	}

	@Override
	public boolean isLocked() {
		return super.isLocked() || !transformable;
	}
}
