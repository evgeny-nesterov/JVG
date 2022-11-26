package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.Node;

public abstract class NodeExpression extends Node {
	public NodeExpression(String name, int type) {
		super(name, type);
	}

	public NodeExpression(String name, int type, int line) {
		super(name, type, line);
	}

	// TODO
	// public Type getExpressionType();
}
