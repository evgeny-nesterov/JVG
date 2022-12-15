package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeBoolean extends Node {
	private final static String name = "boolean";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private final static NodeBoolean TRUE = new NodeBoolean(true);

	private final static NodeBoolean FALSE = new NodeBoolean(false);

	public static NodeBoolean getInstance(boolean value) {
		return value ? TRUE : FALSE;
	}

	private NodeBoolean(boolean value) {
		super(name, TYPE_BOOLEAN);
		this.value = value;
	}

	private boolean value;

	@Override
	public boolean isValue() {
		return true;
	}

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.BOOLEAN;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.bool = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeBoolean(value);
	}

	public static NodeBoolean decode(DecodeContext os) throws IOException {
		return getInstance(os.readBoolean());
	}
}
