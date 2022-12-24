package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeFor extends HiNode {
	public NodeFor(HiNode initialization, NodeExpression condition, HiNode assignment, HiNode body) {
		super("for", TYPE_FOR);
		this.initialization = initialization;
		this.condition = condition;
		this.assignment = assignment;
		this.body = body;
	}

	private HiNode initialization;

	private NodeExpression condition;

	private HiNode assignment;

	private HiNode body;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = true;
		ctx.enter(RuntimeContext.FOR, this);
		if (initialization != null) {
			valid &= initialization.validate(validationInfo, ctx);
			if (initialization instanceof NodeDeclaration && ((NodeDeclaration) initialization).modifiers.hasModifiers()) {
				validationInfo.error("modifiers not allowed", initialization.getToken());
				valid = false;
			}
		}
		if (condition != null) {
			valid &= condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		}
		if (assignment != null) {
			valid &= assignment.validate(validationInfo, ctx);
		}
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enter(RuntimeContext.FOR, token);
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
		return new NodeFor(os.readNullable(HiNode.class), (NodeExpression) os.readNullable(HiNode.class), os.readNullable(HiNode.class), os.readNullable(HiNode.class));
	}
}
