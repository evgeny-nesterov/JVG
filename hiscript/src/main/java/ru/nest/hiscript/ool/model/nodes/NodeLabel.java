package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;

public class NodeLabel extends Node {
	public NodeLabel(String label, Node statement) {
		super("label", TYPE_LABEL);
		this.label = label.intern();
		this.statement = statement;
	}

	private String label;

	private Node statement;

	@Override
	public void execute(RuntimeContext ctx) {
		if (statement != null) {
			ctx.enterLabel(label, token);
			try {
				statement.execute(ctx);
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(label);
		os.writeNullable(statement);
	}

	public static NodeLabel decode(DecodeContext os) throws IOException {
		return new NodeLabel(os.readNullableUTF(), os.readNullable(Node.class));
	}
}
