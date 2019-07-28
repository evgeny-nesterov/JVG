package script.pol.model;

import script.tokenizer.Words;

public class StringNode extends Node implements Value {
	public StringNode(String value) {
		super("string");
		this.value = value;
	}

	private String value;

	public String getString() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.STRING;
		ctx.value.dimension = 0;
		ctx.value.string = value;
	}
}
