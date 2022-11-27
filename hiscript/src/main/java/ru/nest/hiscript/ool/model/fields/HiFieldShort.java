package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldShort extends HiFieldNumber<Short> {
	public HiFieldShort(String name) {
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
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
				break;
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

			default:
				// autocast
				if (value.valueType == Value.VALUE && valueType == INT) {
					if (value.intNumber >= Short.MIN_VALUE && value.intNumber <= Short.MAX_VALUE) {
						this.value = (short) value.intNumber;
						return;
					}
				}
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
				break;
		}
	}

	@Override
	public Short get() {
		return value;
	}
}
