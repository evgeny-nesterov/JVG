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
	public void compile() throws ExecuteException {
		// do nothing
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		// do nothing
	}
}
