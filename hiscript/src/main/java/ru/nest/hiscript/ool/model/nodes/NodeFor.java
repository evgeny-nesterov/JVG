package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

public class NodeFor extends Node {
	public NodeFor(Node initialization, NodeExpression condition, Node assignment, Node body) {
		super("for", TYPE_FOR);
		this.initialization = initialization;
		this.condition = condition;
		this.assignment = assignment;
		this.body = body;
	}

	private Node initialization;

	private NodeExpression condition;

	private Node assignment;

	private Node body;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enter(RuntimeContext.FOR, line);
		try {
			if (initialization != null) {
				initialization.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
			}

			while (true) {
				if (condition != null) {
					condition.execute(ctx);
					if (ctx.exitFromBlock()) {
						return;
					}

					boolean is = ctx.value.getBoolean();
					if (ctx.exitFromBlock()) {
						return;
					}

					if (!is) {
						break;
					}
				}

				if (body != null) {
					body.execute(ctx);
					if (ctx.exitFromBlock()) {
						return;
					}

					if (ctx.isBreak || (ctx.isContinue && !ctx.isCurrentLabel())) {
						break;
					}
				}

				if (assignment != null) {
					assignment.execute(ctx);
					if (ctx.exitFromBlock()) {
						return;
					}
				}
			}
		} finally {
			ctx.exit();
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(initialization);
		os.writeNullable(condition);
		os.writeNullable(assignment);
		os.writeNullable(body);
	}

	public static NodeFor decode(DecodeContext os) throws IOException {
		return new NodeFor(os.readNullable(Node.class), (NodeExpression) os.readNullable(Node.class), os.readNullable(Node.class), os.readNullable(Node.class));
	}
}
