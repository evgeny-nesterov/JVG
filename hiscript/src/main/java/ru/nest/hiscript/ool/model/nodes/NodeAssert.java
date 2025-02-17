package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.ool.runtime.RuntimeContext;

import java.io.IOException;

public class NodeAssert extends HiNode {
	public NodeAssert(NodeExpression conditionNode, NodeExpression messageNode) {
		super("assert", TYPE_ASSERT, true);
		this.conditionNode = conditionNode;
		this.messageNode = messageNode;
	}

	private final NodeExpression conditionNode;

	private final NodeExpression messageNode;

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		boolean valid = conditionNode.validate(validationInfo, ctx) && conditionNode.expectBooleanValue(validationInfo, ctx);
		valid &= ctx.level.checkUnreachable(validationInfo, getToken());
		if (messageNode != null) {
			HiClass stringClass = HiClass.forName(ctx, HiClass.STRING_CLASS_NAME);
			valid &= messageNode.validate(validationInfo, ctx) && messageNode.expectValueClass(validationInfo, ctx, stringClass);
		}
		return valid;
	}

	@Override
	public void execute(RuntimeContext ctx) {
		conditionNode.execute(ctx);
		if (ctx.exitFromBlock()) {
			return;
		}

		boolean value = ctx.value.getBoolean();
		if (ctx.exitFromBlock()) {
			return;
		}

		if (!value) {
			String message;
			if (messageNode != null) {
				messageNode.execute(ctx);
				if (ctx.exitFromBlock()) {
					return;
				}
				message = ctx.value.getStringValue(ctx);
			} else {
				message = "Assert failed";
			}
			ctx.throwException("AssertException", message);
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.write(conditionNode);
		os.writeNullable(messageNode);
	}

	public static NodeAssert decode(DecodeContext os) throws IOException {
		return new NodeAssert(os.read(NodeExpression.class), os.readNullable(NodeExpression.class));
	}
}
