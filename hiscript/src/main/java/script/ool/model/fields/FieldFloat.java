package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class FieldFloat extends FieldNumber<Float> {
	public FieldFloat(String name) {
		super("float", name);
	}

	private float value;

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case FLOAT:
				value.floatNumber = this.value;
				break;

			case DOUBLE:
				value.doubleNumber = this.value;
				break;

			default:
				// error
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
				// error
				break;
		}
	}

	@Override
	public Float get() {
		return value;
	}
}
