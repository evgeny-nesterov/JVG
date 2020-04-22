package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

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
			ctx.enter(RuntimeContext.DOWHILE, line);
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
		return new NodeDoWhile(os.readNullable(NodeBlock.class), os.read(NodeExpression.class));
	}
}
