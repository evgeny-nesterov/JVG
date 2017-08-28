package script.ool.model.fields;

import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class FieldByte extends FieldNumber<Byte> {
	public FieldByte(String name) {
		super("byte", name);
	}

	private byte value;

	public void get(RuntimeContext ctx, Value value, int valueType) {
		switch (valueType) {
			case BYTE:
				value.byteNumber = this.value;
				break;

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
		if (valueType != BYTE) {
			ctx.throwException("incompatible types; found " + value.type.fullName + ", required " + type.name);
			return;
		}

		this.value = value.byteNumber;
	}

	public Byte get() {
		return value;
	}
}
