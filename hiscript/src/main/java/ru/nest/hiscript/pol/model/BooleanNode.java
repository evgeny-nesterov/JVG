package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class BooleanNode extends Node {
	public BooleanNode(boolean value) {
		super("boolean");
		this.value = value;
	}

	private final boolean value;

	public boolean getBoolean() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = Words.BOOLEAN;
		ctx.value.dimension = 0;
		ctx.value.bool = value;
	}
}
