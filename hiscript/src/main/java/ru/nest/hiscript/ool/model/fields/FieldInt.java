package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class FieldInt extends FieldNumber<Integer> {
	public FieldInt(String name) {
		super("int", name);
	}

	private int value;

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
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
			case FLOAT:
			case DOUBLE:
				// error
				break;
		}
	}

	@Override
	public Integer get() {
		return value;
	}
}
