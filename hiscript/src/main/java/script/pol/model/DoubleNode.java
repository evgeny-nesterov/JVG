package script.pol.model;

import script.tokenizer.Words;

public class DoubleNode extends Node implements Value {
	public DoubleNode(double value) {
		super("double");
		this.value = value;
	}

	private double value;

	public double getNumber() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.DOUBLE;
		ctx.value.dimension = 0;
		ctx.value.doubleNumber = value;
	}
}
