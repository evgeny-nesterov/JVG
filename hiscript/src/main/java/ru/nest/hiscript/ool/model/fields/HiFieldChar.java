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
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
				break;
		}
	}

	@Override
	public void set(RuntimeContext ctx, Value value, int valueType) {
		if (valueType == CHAR) {
			this.value = value.character;
		} else {
			// autocast
			if (value.valueType == Value.VALUE) {
				switch (valueType) {
					case BYTE:
						if (value.byteNumber >= Character.MIN_VALUE) {
							this.value = (char) value.byteNumber;
							return;
						}
						break;

					case SHORT:
						if (value.shortNumber >= Character.MIN_VALUE && value.shortNumber <= Character.MAX_VALUE) {
							this.value = (char) value.shortNumber;
							return;
						}
						break;

					case INT:
						if (value.intNumber >= Character.MIN_VALUE && value.intNumber <= Character.MAX_VALUE) {
							this.value = (char) value.intNumber;
							return;
						}
						break;
				}
			}
			ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
		}
	}

	@Override
	public Character get() {
		return value;
	}
}
