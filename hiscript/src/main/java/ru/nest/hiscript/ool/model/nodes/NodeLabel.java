package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeLabel extends Node {
	public NodeLabel(String label, Node statement) {
		super("label", TYPE_LABEL);
		this.label = label.intern();
		this.statement = statement;
	}

	private String label;

	private Node statement;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (statement != null) {
			return statement.validate(validationInfo, ctx);
		}
		return true;
	}

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
