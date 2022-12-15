package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeInt extends NodeNumber {
	private final static String name = "int";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeInt(int value, boolean hasSign) {
		super(name, TYPE_INT, hasSign);
		this.value = value;
	}

	private int value;

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.INT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.intNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeInt(value);
		os.writeBoolean(hasSign);
	}

	public static NodeInt decode(DecodeContext os) throws IOException {
		return new NodeInt(os.readInt(), os.readBoolean());
	}
}
