package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeChar extends Node {
	private final static String name = "char";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	private static NodeChar[] cache = new NodeChar[256];

	public static NodeChar getInstance(char value) {
		int index = value & 0xFF;
		if (cache[index] == null) {
			cache[index] = new NodeChar(value);
		}
		return cache[index];
	}

	private NodeChar(char value) {
		super(name, TYPE_CHAR);
		this.value = value;
	}

	private char value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.character = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeChar(value);
	}

	public static NodeChar decode(DecodeContext os) throws IOException {
		return getInstance(os.readChar());
	}
}
