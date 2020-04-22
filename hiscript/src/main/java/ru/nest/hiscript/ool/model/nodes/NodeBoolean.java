package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeBoolean extends Node {
	private final static String name = "boolean";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

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
