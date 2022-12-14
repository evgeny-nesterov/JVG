package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.io.IOException;

public class NodeDoWhile extends Node {
	public NodeDoWhile(NodeBlock body, NodeExpression condition) {
		super("do-while", TYPE_DO_WHILE);
		this.body = body;
		this.condition = condition;
	}

	private NodeBlock body;

	private NodeExpression condition;

	@Override
	public void execute(RuntimeContext ctx) {
		boolean is;
		do {
			ctx.enter(RuntimeContext.DO_WHILE, token);
			try {
				if (body != null) {
					body.execute(ctx);
					if (ctx.exitFromBlock()) {
						return;
					}

					if (ctx.isBreak || (ctx.isContinue && !ctx.isCurrentLabel())) {
						break;
					}
				}
			} finally {
				ctx.exit();
			}

			condition.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			is = ctx.value.getBoolean();
			if (ctx.exitFromBlock()) {
				return;
			}
		} while (is);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(body);
		os.write(condition);
	}

	public static NodeDoWhile decode(DecodeContext os) throws IOException {
		return new NodeDoWhile((NodeBlock) os.readNullable(Node.class), (NodeExpression) os.read(Node.class));
	}
}
