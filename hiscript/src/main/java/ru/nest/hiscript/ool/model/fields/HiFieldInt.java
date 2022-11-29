package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class HiFieldInt extends HiFieldNumber<Integer> {
	public HiFieldInt(String name) {
		super("int", name);
	}

	public HiFieldInt(String name, int value) {
		super("int", name);
		this.value = value;
		this.initialized = true;
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
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
				break;
		}
	}

	public void set(int value) {
		this.value = value;
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

			default:
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.name);
				break;
		}
	}

	@Override
	public Integer get() {
		return value;
	}
}
