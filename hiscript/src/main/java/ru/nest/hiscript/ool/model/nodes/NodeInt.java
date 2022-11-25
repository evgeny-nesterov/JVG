package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeInt extends Node {
	private final static String name = "int";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeInt(int value) {
		super(name, TYPE_INT);
		this.value = value;
	}

	private int value;

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
	}

	public static NodeInt decode(DecodeContext os) throws IOException {
		return new NodeInt(os.readInt());
	}
}
