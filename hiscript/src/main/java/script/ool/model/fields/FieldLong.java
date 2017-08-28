package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class FieldLong extends FieldNumber<Long> {
	public FieldLong(String name) {
		super("long", name);
	}

	private long value;

	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
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
			case DOUBLE:
				// error
				break;
		}
	}

	public Long get() {
		return value;
	}
}
