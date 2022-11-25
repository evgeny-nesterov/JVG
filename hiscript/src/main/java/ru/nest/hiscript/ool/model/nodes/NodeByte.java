package ru.nest.hiscript.ool.model.nodes;

import java.io.IOException;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

public class NodeByte extends Node {
	private final static String name = "byte";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private static NodeByte[] cache;

	public static NodeByte getInstance(byte value) {
		if (cache == null) {
			cache = new NodeByte[256];
		}

		int index = value & 0xFF;
		if (cache[index] == null) {
			cache[index] = new NodeByte(value);
		}

		return cache[index];
	}

	private NodeByte(byte value) {
		super(name, TYPE_BYTE);
		this.value = value;
	}

	private byte value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.byteNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeByte(value);
	}

	public static NodeByte decode(DecodeContext os) throws IOException {
		return getInstance(os.readByte());
	}
}
