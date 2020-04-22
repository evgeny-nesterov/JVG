package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class FloatNode extends Node implements Value {
	public FloatNode(float value) {
		super("float");
		this.value = value;
	}

	private float value;

	public float getNumber() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.FLOAT;
		ctx.value.dimension = 0;
		ctx.value.floatNumber = value;
	}
}
