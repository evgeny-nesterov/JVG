package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeDouble extends NodeNumber {
	private final static String name = "double";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	// TODO NaN, Infinite

	public NodeDouble(double value, boolean hasSign) {
		super(name, TYPE_DOUBLE, hasSign);
		this.value = value;
	}

	private double value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.doubleNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeDouble(value);
		os.writeBoolean(hasSign);
	}

	public static NodeDouble decode(DecodeContext os) throws IOException {
		return new NodeDouble(os.readDouble(), os.readBoolean());
	}
}
