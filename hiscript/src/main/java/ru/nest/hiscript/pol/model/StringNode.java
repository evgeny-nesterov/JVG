package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class StringNode extends Node implements Value {
	public StringNode(String value) {
		super("string");
		this.value = value;
	}

	private final String value;

	public String getString() {
		return value;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.type = WordType.STRING;
		ctx.value.dimension = 0;
		ctx.value.string = value;
	}
}
