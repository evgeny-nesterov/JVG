package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

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
	public void get(RuntimeContext ctx, Value value, int valueType) {
		value.intNumber = this.value;
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
