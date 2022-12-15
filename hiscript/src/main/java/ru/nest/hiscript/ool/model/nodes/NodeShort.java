package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassPrimitive;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeShort extends NodeNumber {
	private final static String name = "short";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeShort(short value, boolean hasSign) {
		super(name, TYPE_SHORT, hasSign);
		this.value = value;
	}

	private short value;

	@Override
	public HiClass getValueType(ValidationInfo validationInfo, CompileClassContext ctx) {
		return HiClassPrimitive.SHORT;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.shortNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShort(value);
		os.writeBoolean(hasSign);
	}

	public static NodeShort decode(DecodeContext os) throws IOException {
		return new NodeShort(os.readShort(), os.readBoolean());
	}
}
