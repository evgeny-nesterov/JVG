package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class FieldDouble extends FieldNumber<Double> {
	public FieldDouble(String name) {
		super("double", name);
	}

	private double value;

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		if (valueType != DOUBLE) {
			// error
		}
		value.doubleNumber = this.value;
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
}
