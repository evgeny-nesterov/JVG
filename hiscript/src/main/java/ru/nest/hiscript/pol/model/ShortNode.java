package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class ShortNode extends Node implements Value {
	public ShortNode(short value) {
		super("int");
		this.value = value;
	}

	private short value;

	public short getNumber() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.SHORT;
		ctx.value.dimension = 0;
		ctx.value.shortNumber = value;
	}
}
