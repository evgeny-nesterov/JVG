package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeFloat extends NodeNumber {
	private final static String name = "float";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	// TODO NaN, Infinite

	public NodeFloat(float value, boolean hasSign) {
		super(name, TYPE_FLOAT, hasSign);
		this.value = value;
	}

	private float value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.floatNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeFloat(value);
		os.writeBoolean(hasSign);
	}

	public static NodeFloat decode(DecodeContext os) throws IOException {
		return new NodeFloat(os.readFloat(), os.readBoolean());
	}
}
