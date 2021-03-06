package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeReturn extends Node {
	public NodeReturn(Node value) {
		super("return", TYPE_RETURN);
		this.value = value;
	}

	private Node value;

	@Override
	public void execute(RuntimeContext ctx) {
		try {
			if (value != null) {
				value.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				// TODO: check on void return value
			} else {
				ctx.value.valueType = Value.VALUE;
				ctx.value.type = Clazz.getPrimitiveClass("void");
			}
		} finally {
			ctx.isReturn = true;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(value);
	}

	public static NodeReturn decode(DecodeContext os) throws IOException {
		return new NodeReturn(os.readNullable(Node.class));
	}
}
