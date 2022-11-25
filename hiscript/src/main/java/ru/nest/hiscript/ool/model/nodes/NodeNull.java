package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;
import ru.nest.hiscript.ool.model.classes.HiClassNull;

public class NodeNull extends Node {
	public final static NodeNull instance = new NodeNull();

	private NodeNull() {
		super("null", TYPE_NULL);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = HiClassNull.NULL;
		ctx.value.object = null;
		ctx.value.array = null;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeNull decode(DecodeContext os) throws IOException {
		return instance;
	}
}
