package ru.nest.hiscript.ool.model.fields;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.nodes.NodeValueType;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;
import ru.nest.hiscript.ool.runtime.Value;

public class HiFieldBoolean extends HiFieldPrimitive<Boolean> {
	public HiFieldBoolean(String name) {
		super(Type.getPrimitiveType("boolean"), name);
	}

	private boolean value;

	@Override
	protected boolean validateType(ValidationInfo validationInfo, CompileClassContext ctx, HiClass fieldClass, NodeValueType valueType) {
		return valueType.clazz == HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void get(RuntimeContext ctx, Value value) {
		value.valueType = Value.VALUE;
		value.valueClass = getClass(ctx);
		value.bool = this.value;
	}

	@Override
	public void set(RuntimeContext ctx, Value value) {
		declared = true;

		if (value.valueClass.getAutoboxedPrimitiveClass() == HiClassPrimitive.BOOLEAN) {
			// @autoboxing
			value.substitutePrimitiveValueFromAutoboxValue();
		}

		this.value = value.bool;
		this.initialized = true;
	}

	@Override
	public Boolean get() {
		return value;
	}

	@Override
	public Object getJava(RuntimeContext ctx) {
		return value;
	}
}
