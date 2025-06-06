package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeWhile extends HiNode {
	public NodeWhile(HiNode condition, HiNode body) {
		super("while", TYPE_WHILE, true);
		this.condition = condition;
		this.body = body;
	}

	private final HiNode condition;

	private final HiNode body;

	@Override
	public NodeReturn getReturnNode() {
		return body != null ? body.getReturnNode() : null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		ctx.enter(ContextType.WHILE, this);
		try {
			valid &= condition != null && condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
			valid &= body == null || body.validateBlock(validationInfo, ctx);
		} finally {
			ctx.exit();
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		condition.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		while (ctx.value.getBoolean()) {
			if (ctx.exitFromBlock()) {
				return;
			}

			ctx.enter(ContextType.WHILE, token);
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

				condition.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
			} finally {
				ctx.exit();
			}
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(condition);
		os.writeNullable(body);
	}

	public static NodeWhile decode(DecodeContext os) throws IOException {
		return new NodeWhile(os.read(HiNode.class), os.readNullable(HiNode.class));
	}
}
