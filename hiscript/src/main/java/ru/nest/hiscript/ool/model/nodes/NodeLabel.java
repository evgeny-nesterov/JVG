package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.Set;

import static ru.nest.hiscript.ool.model.nodes.NodeVariable.*;

public class NodeLabel extends HiNode {
	public NodeLabel(String label, HiNode statement) {
		super("label", TYPE_LABEL, false);
		this.label = label.intern();
		this.statement = statement;
	}

	private final String label;

	private final HiNode statement;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		return statement != null && statement.isReturnStatement(label, labels);
	}

	@Override
	public NodeReturn getReturnNode() {
		return statement != null ? statement.getReturnNode() : null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		if (statement != null) {
			// @unnamed
			if (UNNAMED.equals(label)) {
				validationInfo.error("keyword '_' cannot be used as an identifier", token);
				valid = false;
			}

			CompileClassContext.CompileClassLevel level = ctx.level.getLabelLevel(label);
			if (level != null) {
				validationInfo.error("label '" + label + "' already in use", token);
			}

			ctx.enterLabel(label, this);
			try {
				valid &= statement.validate(validationInfo, ctx);
			} finally {
				ctx.exit();
			}
		}
		return valid;
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
		return new NodeLabel(os.readNullableUTF(), os.readNullable(HiNode.class));
	}
}
