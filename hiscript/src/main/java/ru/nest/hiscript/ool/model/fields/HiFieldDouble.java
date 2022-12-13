package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldDouble extends HiFieldNumber<Double> {
	public HiFieldDouble(String name) {
		super("double", name);
	}

	private double value;

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		if (valueType == DOUBLE) {
			value.doubleNumber = this.value;
		} else {
			ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.fullName);
		}
	}

	@Override
	public void set(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case CHAR:
				this.value = value.character;
				break;

			case BYTE:
				this.value = value.byteNumber;
				break;

			case SHORT:
				this.value = value.shortNumber;
				break;

			case INT:
				this.value = value.intNumber;
				break;

			case LONG:
				this.value = value.longNumber;
				break;

			case FLOAT:
				this.value = value.floatNumber;
				break;

			case DOUBLE:
				this.value = value.doubleNumber;
				break;
		}
	}

	@Override
	public Double get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
