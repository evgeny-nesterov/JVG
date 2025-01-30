package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;
import ru.nest.hiscript.tokenizer.Token;

public abstract class NodeExpression extends HiNode {
	public NodeExpression(String name, int type) {
		super(name, type, false);
	}

	public NodeExpression(String name, int type, Token token) {
		super(name, type, token, false);
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		ctx.currentNode = this;
		return NodeReturn.validateLambdaReturn(validationInfo, ctx, this, token);
	}

	public NodeCastedIdentifier checkCastedIdentifier() {
		if (this instanceof NodeExpressionNoLS) {
			NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) this;
			return exprCaseValueNode.checkCastedIdentifier();
		} else {
			return null;
		}
	}

	public boolean isCastedIdentifier() {
		if (this instanceof NodeExpressionNoLS) {
			NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) this;
			return exprCaseValueNode.checkCastedIdentifier() != null;
		} else {
			return false;
		}
	}

	public HiMethod checkMethod() {
		if (this instanceof NodeExpressionNoLS) {
			NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) this;
			return exprCaseValueNode.checkMethod();
		} else {
			return null;
		}
	}
}
