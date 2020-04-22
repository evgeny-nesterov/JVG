package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeLong extends Node {
	private final static String name = "long";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeLong(long value) {
		super(name, TYPE_LONG);
		this.value = value;
	}

	private long value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.longNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeLong(value);
	}

	public static NodeLong decode(DecodeContext os) throws IOException {
		return new NodeLong(os.readLong());
	}
}
