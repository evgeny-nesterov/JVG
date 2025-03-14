package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.PrimitiveType;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

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
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return valueType.clazz == HiClassPrimitive.BYTE || valueType.clazz == HiClassPrimitive.SHORT || valueType.clazz == HiClassPrimitive.CHAR;
	}

	@Override
	public void getPrimitiveValue(RuntimeContext ctx, Value value) {
		value.intNumber = this.value;
	}

	@Override
	public void set(RuntimeContext ctx, Value value, PrimitiveType valueType) {
		switch (valueType) {
			case CHAR_TYPE:
				this.value = value.character;
				break;
			case BYTE_TYPE:
				this.value = value.byteNumber;
				break;
			case SHORT_TYPE:
				this.value = value.shortNumber;
				break;
			case INT_TYPE:
				this.value = value.intNumber;
				break;
		}
	}

	@Override
	public Integer get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
