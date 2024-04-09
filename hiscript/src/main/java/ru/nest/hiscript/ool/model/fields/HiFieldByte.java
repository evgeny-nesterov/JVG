package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiFieldByte extends HiFieldNumber<Byte> {
	public HiFieldByte(String name) {
		super("byte", name);
	}

	private byte value;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		if (valueType.isCompileValue()) {
			if (valueType.type == HiClassPrimitive.INT) {
				return valueType.intValue >= Byte.MIN_VALUE && valueType.intValue <= Byte.MAX_VALUE;
			} else if (valueType.type == HiClassPrimitive.SHORT) {
				return valueType.shortValue >= Byte.MIN_VALUE && valueType.shortValue <= Byte.MAX_VALUE;
			} else if (valueType.type == HiClassPrimitive.CHAR) {
				return valueType.charValue <= Byte.MAX_VALUE;
			}
		}
		return false;
	}

	@Override
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
				ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.fullName);
				break;
		}
	}

	@Override
	public void set(RuntimeContext ctx, Value value, int valueType) {
		if (valueType == BYTE) {
			this.value = value.byteNumber;
		} else {
			// auto-cast
			if (value.valueType == Value.VALUE) {
				switch (valueType) {
					case CHAR:
						if (value.character <= Byte.MAX_VALUE) {
							this.value = (byte) value.character;
							return;
						}
						break;
					case SHORT:
						if (value.shortNumber >= Byte.MIN_VALUE && value.shortNumber <= Byte.MAX_VALUE) {
							this.value = (byte) value.shortNumber;
							return;
						}
						break;
					case INT:
						if (value.intNumber >= Byte.MIN_VALUE && value.intNumber <= Byte.MAX_VALUE) {
							this.value = (byte) value.intNumber;
							return;
						}
						break;
				}
			}
			ctx.throwRuntimeException("incompatible types; found " + value.type.fullName + ", required " + type.fullName);
		}
	}

	@Override
	public Byte get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
