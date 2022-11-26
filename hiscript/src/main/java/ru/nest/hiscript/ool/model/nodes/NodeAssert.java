package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

import java.io.IOException;

public class NodeAssert extends Node {
	public NodeAssert(NodeExpression conditionNode, NodeExpression messageNode) {
		super("assert", TYPE_ASSERT);
		this.conditionNode = conditionNode;
		this.messageNode = messageNode;
	}

	private Node conditionNode;

	private NodeExpression messageNode;

	@Override
	public void execute(RuntimeContext ctx) {
		conditionNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		boolean value = ctx.value.getBoolean();
		if (ctx.exitFromBlock()) {
			return;
		}

		if (!value) {
			String message;
			if (messageNode != null) {
				messageNode.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
				message = ctx.value.getString();
			} else {
				message = "Assert failed";
			}
			ctx.throwException("AssertException", message);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(conditionNode);
		os.writeNullable(messageNode);
	}

	public static NodeAssert decode(DecodeContext os) throws IOException {
		NodeAssert node = new NodeAssert(os.read(NodeExpression.class), os.readNullable(NodeExpression.class));
		return node;
	}
}
