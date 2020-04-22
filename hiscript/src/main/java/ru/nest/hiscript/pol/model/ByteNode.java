package ru.nest.hiscript.pol.model;

import ru.nest.hiscript.tokenizer.Words;

public class ByteNode extends Node implements Value {
	public ByteNode(byte value) {
		super("byte");
		this.value = value;
	}

	private byte value;

	public byte getNumber() {
		return value;
	}

	@Override
	public void compile() throws ExecuteException {
	}

	@Override
	public void execute(RuntimeContext ctx) throws ExecuteException {
		ctx.value.type = Words.BYTE;
		ctx.value.dimension = 0;
		ctx.value.byteNumber = value;
	}
}
