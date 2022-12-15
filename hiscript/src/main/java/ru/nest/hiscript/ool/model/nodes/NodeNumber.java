package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.tokenizer.Token;

public abstract class NodeNumber extends Node {
	protected boolean hasSign;

	public boolean hasSign() {
		return hasSign;
	}

	public NodeNumber(String name, int type, boolean hasSign, Token token) {
		super(name, type, token);
		this.hasSign = hasSign;
	}

	@Override
	public boolean isValue() {
		return true;
	}
}
