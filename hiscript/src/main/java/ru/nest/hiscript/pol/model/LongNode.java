package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class LongNode extends Node implements Value {
	public LongNode(long value) {
		super("long");
		this.value = value;
	}

	private final long value;

	public long getNumber() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = Words.LONG;
		ctx.value.dimension = 0;
		ctx.value.longNumber = value;
	}
}
