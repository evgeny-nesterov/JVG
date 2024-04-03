package ru.nest.hiscript.pol.model;

public class CastNode extends Node implements Value {
	public CastNode(int type, int dimension) {
		super("cast");
		this.type = type;
		this.dimension = dimension;
	}

	private final int type;

	public int getType() {
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
