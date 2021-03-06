package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

public class NodeThrow extends Node {
	public NodeThrow(Node exception) {
		super("throw", TYPE_THROW);
		this.exception = exception;
	}

	private Node exception;

	@Override
	public void execute(RuntimeContext ctx) {
		exception.execute(ctx);
		ctx.exception = ctx.value.getObject();
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		exception.code(os);
	}

	public static NodeThrow decode(DecodeContext os) throws IOException {
		return new NodeThrow(os.read(Node.class));
	}
}
