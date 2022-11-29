package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiObject;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.io.IOException;

public class NodeTry extends Node {
	public NodeTry(Node body, NodeCatch[] catches, Node finallyBody) {
		super("try", TYPE_TRY);
		this.body = body;
		this.catches = catches;
		this.finallyBody = finallyBody;
	}

	private Node body;

	private NodeCatch[] catches;

	private Node finallyBody;

	@Override
	public void execute(RuntimeContext ctx) {
		if (body != null) {
			ctx.enter(RuntimeContext.TRY, line);
			try {
				body.execute(ctx);
			} finally {
				ctx.exit();
			}
		}

		if (ctx.exception != null && !ctx.exception.clazz.name.equals("AssertException") && catches != null) {
			for (NodeCatch catchNode : catches) {
				int index = catchNode.getMatchedExceptionClass(ctx);
				if (index == -2) {
					// error occurred while catch exception class resolving
					return;
				}

				if (index >= 0) {
					catchNode.execute(ctx);
					break;
				}
			}
		}

		if (finallyBody != null) {
			ctx.enter(RuntimeContext.FINALLY, line);
			try {
				finallyBody.execute(ctx);
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(body);
		os.writeShort(catches != null ? catches.length : 0);
		os.writeNullable(catches);
		os.writeNullable(finallyBody);
	}

	public static NodeTry decode(DecodeContext os) throws IOException {
		return new NodeTry(os.readNullable(Node.class), os.readNullableArray(NodeCatch.class, os.readShort()), os.readNullable(Node.class));
	}
}
