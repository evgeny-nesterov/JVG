package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldChar extends HiFieldNumber<Character> {
	public HiFieldChar(String name) {
		super("char", name);
	}

	private char value;

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case CHAR:
				value.character = this.value;
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
		if (valueType != CHAR) {
			// error
		}
		this.value = value.character;
	}

	@Override
	public Character get() {
		return value;
	}
}
