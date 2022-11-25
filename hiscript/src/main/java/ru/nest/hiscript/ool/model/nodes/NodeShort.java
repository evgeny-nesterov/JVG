package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeShort extends Node {
	private final static String name = "short";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeShort(short value) {
		super(name, TYPE_SHORT);
		this.value = value;
	}

	private short value;

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
	}

	public static NodeShort decode(DecodeContext os) throws IOException {
		return new NodeShort(os.readShort());
	}
}
