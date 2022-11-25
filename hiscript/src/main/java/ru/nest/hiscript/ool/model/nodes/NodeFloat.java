package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeFloat extends Node {
	private final static String name = "float";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeFloat(float value) {
		super(name, TYPE_FLOAT);
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
	}

	public static NodeFloat decode(DecodeContext os) throws IOException {
		return new NodeFloat(os.readFloat());
	}
}
