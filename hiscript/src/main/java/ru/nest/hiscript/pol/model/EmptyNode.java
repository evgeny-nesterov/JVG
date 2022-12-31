package ru.nest.hiscript.pol.model;

public class EmptyNode extends Node {
	private final static EmptyNode instance = new EmptyNode();

	public static EmptyNode getInstance() {
		return instance;
	}

	private EmptyNode() {
		super("empty");
	}

	@Override
	public void compile() {
		// do nothing
	}

	@Override
	public void execute(RuntimeContext ctx) {
		// do nothing
	}
}
