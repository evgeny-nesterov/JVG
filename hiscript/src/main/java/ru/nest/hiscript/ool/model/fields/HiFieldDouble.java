package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public class HiFieldDouble extends HiFieldNumber<Double> {
	public HiFieldDouble(String name) {
		super("double", name);
	}

	private double value;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return valueType.clazz.isNumber();
	}

	@Override
	public void get(RuntimeContext ctx, Value value, int valueType) {
		value.doubleNumber = this.value;
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
			case LONG:
				this.value = value.longNumber;
				break;
			case FLOAT:
				this.value = value.floatNumber;
				break;
			case DOUBLE:
				this.value = value.doubleNumber;
				break;
		}
	}

	@Override
	public Double get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
