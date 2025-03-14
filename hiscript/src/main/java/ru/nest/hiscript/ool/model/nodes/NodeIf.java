package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.ContextType;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;
import java.util.Set;

public class NodeIf extends HiNode {
	public NodeIf(NodeExpression condition, HiNode body, NodeIf nextIf) {
		super("if", TYPE_IF, true);
		this.condition = condition;
		this.body = body;
		this.nextIf = nextIf;
	}

	private final NodeExpression condition;

	private final HiNode body;

	private final NodeIf nextIf;

	@Override
	public boolean isReturnStatement(String label, Set<String> labels) {
		if (body != null && !body.isReturnStatement(label, labels)) {
			return false;
		}
		if (nextIf != null) {
			return nextIf.isReturnStatement(label, labels);
		} else if (condition == null) { // else
			return true;
		}
		return false;
	}

	@Override
	public NodeReturn getReturnNode() {
		NodeReturn returnNode = body != null ? body.getReturnNode() : null;
		if (returnNode == null && nextIf != null) {
			returnNode = nextIf.getReturnNode();
		}
		return returnNode;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = ctx.level.checkUnreachable(validationInfo, getToken());
		valid &= validateIf(validationInfo, ctx);
		checkStatementTermination(ctx);
		return valid;
	}

	public boolean validateIf(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.enter(ContextType.IF, this);
		boolean valid = true;
		if (condition != null) {
			valid &= condition.validate(validationInfo, ctx) && condition.expectBooleanValue(validationInfo, ctx);
		}
		valid &= body.validateBlock(validationInfo, ctx);
		if (nextIf != null) {
			valid &= nextIf.validateIf(validationInfo, ctx);
		}
		ctx.exit();
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enter(ContextType.IF, token);
		try {
			if (condition != null) {
				condition.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}

				boolean is = ctx.value.getBoolean();
				if (ctx.exitFromBlock()) {
					return;
				}

				if (is) {
					if (body != null) {
						body.execute(ctx);
					}
					return;
				}
			} else {
				if (body != null) {
					body.execute(ctx);
				}
				return;
			}
		} finally {
			ctx.exit();
		}

		if (nextIf != null) {
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
