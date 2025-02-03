package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiFieldChar extends HiFieldNumber<Character> {
	public HiFieldChar(String name) {
		super("char", name);
	}

	private char value;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		if (valueType.isCompileValue()) {
			if (valueType.clazz == HiClassPrimitive.INT) {
				return valueType.intValue >= Character.MIN_VALUE && valueType.intValue <= Character.MAX_VALUE;
			} else if (valueType.clazz == HiClassPrimitive.SHORT) {
				return valueType.shortValue >= Character.MIN_VALUE && valueType.shortValue <= Character.MAX_VALUE;
			} else if (valueType.clazz == HiClassPrimitive.BYTE) {
				return valueType.byteValue >= Character.MIN_VALUE;
			}
		}
		return false;
	}

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		value.character = this.value;
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
						if (value.shortNumber >= Character.MIN_VALUE) {
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
		}
	}

	@Override
	public Character get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
