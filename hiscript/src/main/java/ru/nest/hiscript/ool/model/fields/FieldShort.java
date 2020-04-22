package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class FieldShort extends FieldNumber<Short> {
	public FieldShort(String name) {
		super("short", name);
	}

	private short value;

	@Override
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

	@Override
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

	@Override
	public Short get() {
		return value;
	}
}
