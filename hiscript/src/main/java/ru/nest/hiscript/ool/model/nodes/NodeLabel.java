package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeLabel extends HiNode {
	public NodeLabel(String label, HiNode statement) {
		super("label", TYPE_LABEL, false);
		this.label = label.intern();
		this.statement = statement;
	}

	private final String label;

	private final HiNode statement;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (statement != null) {
			CompileClassContext.CompileClassLevel level = ctx.level;
			boolean found = false;
			while (level != null) {
				if (level.isLabel(label)) {
					found = true;
					break;
				}
				level = level.parent;
			}
			if (found) {
				validationInfo.error("label '" + label + "' already in use", token);
				return false;
			}

			ctx.enterLabel(label, this);
			try {
				return statement.validate(validationInfo, ctx);
			} finally {
				ctx.exit();
			}
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
		return new NodeLabel(os.readNullableUTF(), os.readNullable(HiNode.class));
	}
}
