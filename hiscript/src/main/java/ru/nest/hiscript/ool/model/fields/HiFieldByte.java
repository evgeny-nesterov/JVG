package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;
import ru.nest.hiscript.ool.runtime.ValueType;

import static ru.nest.hiscript.ool.model.PrimitiveType.*;

public class HiFieldByte extends HiFieldNumber<Byte> {
	public HiFieldByte(String name) {
		super("byte", name);
	}

	private byte value;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		if (valueType.isCompileValue()) {
			if (valueType.clazz == HiClassPrimitive.INT) {
				return valueType.intValue >= Byte.MIN_VALUE && valueType.intValue <= Byte.MAX_VALUE;
			} else if (valueType.clazz == HiClassPrimitive.SHORT) {
				return valueType.shortValue >= Byte.MIN_VALUE && valueType.shortValue <= Byte.MAX_VALUE;
			} else if (valueType.clazz == HiClassPrimitive.CHAR) {
				return valueType.charValue <= Byte.MAX_VALUE;
			}
		}
		return false;
	}

	@Override
	public void getPrimitiveValue(RuntimeContext ctx, Value value) {
		value.byteNumber = this.value;
	}

	@Override
	public void set(RuntimeContext ctx, Value value, PrimitiveType valueType) {
		if (valueType == BYTE_TYPE) {
			this.value = value.byteNumber;
		} else {
			// auto-cast
			if (value.valueType == ValueType.VALUE) {
				switch (valueType) {
					case CHAR_TYPE:
						if (value.character <= Byte.MAX_VALUE) {
							this.value = (byte) value.character;
							return;
						}
						break;
					case SHORT_TYPE:
						if (value.shortNumber >= Byte.MIN_VALUE && value.shortNumber <= Byte.MAX_VALUE) {
							this.value = (byte) value.shortNumber;
							return;
						}
						break;
					case INT_TYPE:
						if (value.intNumber >= Byte.MIN_VALUE && value.intNumber <= Byte.MAX_VALUE) {
							this.value = (byte) value.intNumber;
							return;
						}
						break;
				}
			}
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
