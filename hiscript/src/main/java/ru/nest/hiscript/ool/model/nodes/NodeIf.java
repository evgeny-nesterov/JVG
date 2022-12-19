package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeIf extends HiNode {
	public NodeIf(NodeExpression condition, HiNode body, NodeIf nextIf) {
		super("if", TYPE_IF);
		this.condition = condition;
		this.body = body;
		this.nextIf = nextIf;
	}

	private NodeExpression condition;

	private HiNode body;

	private NodeIf nextIf;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		ctx.enter(RuntimeContext.IF, this);
		if (condition != null) {
			valid &= condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		}
		valid &= body.validateBlock(validationInfo, ctx);
		if (nextIf != null) {
			valid &= nextIf.validate(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

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
			ctx.enter(RuntimeContext.IF, token);
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
		return new NodeIf((NodeExpression) os.readNullable(HiNode.class), os.readNullable(HiNode.class), (NodeIf) os.readNullable(HiNode.class));
	}
}
