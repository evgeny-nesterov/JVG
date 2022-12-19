package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeWhile extends Node {
	public NodeWhile(Node condition, Node body) {
		super("while", TYPE_WHILE);
		this.condition = condition;
		this.body = body;
	}

	private Node condition;

	private Node body;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		boolean valid = condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		valid &= body.validateBlock(validationInfo, ctx);
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

			ctx.enter(RuntimeContext.WHILE, token);
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
		return new NodeWhile(os.read(Node.class), os.readNullable(Node.class));
	}
}
