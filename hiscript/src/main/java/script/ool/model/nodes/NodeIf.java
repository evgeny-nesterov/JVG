package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;

public class NodeIf extends Node {
	public NodeIf(NodeExpression condition, Node body, NodeIf nextIf) {
		super("if", TYPE_IF);
		this.condition = condition;
		this.body = body;
		this.nextIf = nextIf;
	}

	private NodeExpression condition;

	private Node body;

	private NodeIf nextIf;

	@Override
	public void execute(RuntimeContext ctx) {
		if (condition != null) {
			condition.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}
		}

		boolean is = true;
		if (condition != null) {
			is = ctx.value.getBoolean();
			if (ctx.exitFromBlock()) {
				return;
			}
		}

		if (is) {
			ctx.enter(RuntimeContext.IF, line);
			try {
				if (body != null) {
					body.execute(ctx);
				}
			} finally {
				ctx.exit();
			}
		} else if (nextIf != null) {
			nextIf.execute(ctx);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(condition);
		os.writeNullable(body);
		os.writeNullable(nextIf);
	}

	public static NodeIf decode(DecodeContext os) throws IOException {
		return new NodeIf((NodeExpression) os.readNullable(Node.class), os.readNullable(Node.class), (NodeIf) os.readNullable(Node.class));
	}
}
