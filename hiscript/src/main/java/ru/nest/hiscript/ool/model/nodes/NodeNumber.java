package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.tokenizer.Token;

public abstract class NodeNumber extends HiNode {
	protected boolean hasSign;

	public boolean hasSign() {
		return hasSign;
	}

	public NodeNumber(String name, int type, boolean hasSign, Token token) {
		super(name, type, token, false);
		this.hasSign = hasSign;
	}

	@Override
	public NodeValueType.NodeValueReturnType getValueReturnType() {
		return NodeValueType.NodeValueReturnType.compileValue;
	}

	@Override
	public boolean isCompileValue() {
		return true;
	}

	@Override
	public boolean isConstant(CompileClassContext ctx) {
		return true;
	}
}
