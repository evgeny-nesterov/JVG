package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class ReturnNode extends Node {
	public ReturnNode(Node value) {
		super("return");
		this.value = value;

		if (value != null) {
			value.setParent(this);
		}
	}

	private Node value;

	public Node getValue() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
		if (value != null) {
			value.compile();
		}
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.isExit = true;

		if (value != null) {
			value.execute(ctx);
		} else {
			ctx.value.dimension = 0;
			ctx.value.type = Words.VOID;
		}
	}
}
