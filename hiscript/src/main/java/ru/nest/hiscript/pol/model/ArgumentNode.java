package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class ArgumentNode extends Node {
	public ArgumentNode(WordType type, int dimension, String name) {
		super("argument");
		this.type = type;
		this.dimension = dimension;
		this.name = name;
	}

	private final WordType type;

	public WordType getType() {
		return type;
	}

	private final int dimension;

	public int getDimension() {
		return dimension;
	}

	private final String name;

	public String getArgName() {
		return name;
	}

	private Node statement;

	private Variable var;

	@Override
	public void compile() {
		statement = getTopStatement();
		var = new Variable(null, name, type, dimension);
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		statement.addVariable(var);
	}
}
