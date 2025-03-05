package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class FloatNode extends Node implements Value {
	public FloatNode(float value) {
		super("float");
		this.value = value;
	}

	private final float value;

	public float getNumber() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = WordType.FLOAT;
		ctx.value.dimension = 0;
		ctx.value.floatNumber = value;
	}
}
