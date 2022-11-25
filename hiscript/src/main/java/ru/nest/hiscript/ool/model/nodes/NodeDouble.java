package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeDouble extends Node {
	private final static String name = "double";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeDouble(double value) {
		super(name, TYPE_DOUBLE);
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
	}

	public static NodeDouble decode(DecodeContext os) throws IOException {
		return new NodeDouble(os.readDouble());
	}
}
