package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class ShortNode extends Node implements Value {
	public ShortNode(short value) {
		super("int");
		this.value = value;
	}

	private final short value;

	public short getNumber() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = WordType.SHORT;
		ctx.value.dimension = 0;
		ctx.value.shortNumber = value;
	}
}
