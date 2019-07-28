package script.pol.model;

import script.tokenizer.Words;

public class IntNode extends Node implements Value {
	public IntNode(int value) {
		super("int");
		this.value = value;
	}

	private int value;

	public int getNumber() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.INT;
		ctx.value.dimension = 0;
		ctx.value.intNumber = value;
	}
}
