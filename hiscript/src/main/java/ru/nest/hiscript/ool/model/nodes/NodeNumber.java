package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;

public abstract class NodeNumber extends Node {
	protected boolean hasSign;

	public boolean hasSign() {
		return hasSign;
	}

	public NodeNumber(String name, int type, boolean hasSign) {
		super(name, type);
		this.hasSign = hasSign;
	}
}
