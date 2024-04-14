package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;
import java.util.Set;

public class NodeDoWhile extends HiNode {
	public NodeDoWhile(NodeBlock body, NodeExpression condition) {
		super("do-while", TYPE_DO_WHILE, true);
		this.body = body;
		this.condition = condition;
	}

	private final NodeBlock body;

	private final NodeExpression condition;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		return body != null && body.isReturnStatement(label, labels);
	}

	@Override
	public NodeReturn getReturnNode() {
		return body != null ? body.getReturnNode() : null;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		ctx.enter(RuntimeContext.DO_WHILE, this);
		if (body != null) {
			valid &= body.validateBlock(validationInfo, ctx);
		}
		valid &= condition != null && condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		boolean is;
		do {
			ctx.enter(RuntimeContext.DO_WHILE, token);
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
			} finally {
				ctx.exit();
			}

			condition.execute(ctx);
			if (ctx.exitFromBlock()) {
				return;
			}

			is = ctx.value.getBoolean();
			if (ctx.exitFromBlock()) {
				return;
			}
		} while (is);
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullable(body);
		os.write(condition);
	}

	public static NodeDoWhile decode(DecodeContext os) throws IOException {
		return new NodeDoWhile((NodeBlock) os.readNullable(HiNode.class), (NodeExpression) os.read(HiNode.class));
	}
}
