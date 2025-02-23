package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.tokenizer.Token;

public abstract class NodeNumber extends HiNode {
	public NodeNumber(String name, int type, Token token) {
		super(name, type, token, false);
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
