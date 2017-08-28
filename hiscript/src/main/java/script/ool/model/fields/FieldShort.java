package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class FieldShort extends FieldNumber<Short> {
	public FieldShort(String name) {
		super("short", name);
	}

	private short value;

	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case SHORT:
				value.shortNumber = this.value;
				break;

			case INT:
				value.intNumber = this.value;
				break;

			case LONG:
				value.longNumber = this.value;
				break;

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

	public void set(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case BYTE:
				this.value = value.byteNumber;
				break;

			case SHORT:
				this.value = value.shortNumber;
				break;

			case CHAR:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
				// error
				break;
		}
	}

	public Short get() {
		return value;
	}
}
