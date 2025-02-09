package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

public abstract class NodeExpression extends HiNode {
	public NodeExpression(String name, int type) {
		super(name, type, false);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		return NodeReturn.validateLambdaReturn(validationInfo, ctx, this, token);
	}

	public NodeCastedIdentifier checkCastedIdentifier() {
		return null;
	}

	public boolean isCastedIdentifier() {
		return false;
	}
}
