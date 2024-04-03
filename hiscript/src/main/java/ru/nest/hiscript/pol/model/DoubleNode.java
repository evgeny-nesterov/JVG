package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class DoubleNode extends Node implements Value {
	public DoubleNode(double value) {
		super("double");
		this.value = value;
	}

	private final double value;

	public double getNumber() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = Words.DOUBLE;
		ctx.value.dimension = 0;
		ctx.value.doubleNumber = value;
	}
}
