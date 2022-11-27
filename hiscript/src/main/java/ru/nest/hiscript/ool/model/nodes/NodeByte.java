package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeByte extends NodeNumber {
	private final static String name = "byte";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	private static NodeByte[] cache;

	public static NodeByte getInstance(byte value, boolean hasSign) {
		if (cache == null) {
			cache = new NodeByte[256];
		}

		int index = value & 0xFF;
		if (cache[index] == null) {
			cache[index] = new NodeByte(value, hasSign);
		}

		return cache[index];
	}

	private NodeByte(byte value, boolean hasSign) {
		super(name, TYPE_BYTE, hasSign);
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
		os.writeBoolean(hasSign);
	}

	public static NodeByte decode(DecodeContext os) throws IOException {
		return getInstance(os.readByte(), os.readBoolean());
	}

	@Override
	public String toString() {
		return super.name + "=" + value;
	}
}
