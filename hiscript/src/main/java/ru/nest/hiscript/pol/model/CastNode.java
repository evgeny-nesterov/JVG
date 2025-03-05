package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.WordType;

public class CastNode extends Node implements Value {
	public CastNode(WordType type, int dimension) {
		super("cast");
		this.type = type;
		this.dimension = dimension;
	}

	private final WordType type;

	public WordType getType() {
		return type;
	}

	private final int dimension;

	public int getDimension() {
		return dimension;
	}

	@Override
	public void compile() {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.cast(type, dimension);
	}
}
