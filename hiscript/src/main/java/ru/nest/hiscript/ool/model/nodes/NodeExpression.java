package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.tokenizer.Token;

public abstract class NodeExpression extends HiNode {
	public NodeExpression(String name, int type) {
		super(name, type);
	}

	public NodeExpression(String name, int type, Token token) {
		super(name, type, token);
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
}
