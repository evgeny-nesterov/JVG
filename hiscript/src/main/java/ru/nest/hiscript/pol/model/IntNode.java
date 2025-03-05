package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class IntNode extends Node implements Value {
	public IntNode(int value) {
		super("int");
		this.value = value;
	}

	private final int value;

	public int getNumber() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = WordType.INT;
		ctx.value.dimension = 0;
		ctx.value.intNumber = value;
	}
}
