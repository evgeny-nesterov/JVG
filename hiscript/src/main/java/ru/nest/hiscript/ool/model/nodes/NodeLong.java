package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Value;

import java.io.IOException;

public class NodeLong extends NodeNumber {
	private final static String name = "long";

	private final static HiClass type = HiClass.getPrimitiveClass(name);

	public NodeLong(long value, boolean hasSign) {
		super(name, TYPE_LONG, hasSign);
		this.value = value;
	}

	private long value;

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.longNumber = value;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeLong(value);
		os.writeBoolean(hasSign);
	}

	public static NodeLong decode(DecodeContext os) throws IOException {
		return new NodeLong(os.readLong(), os.readBoolean());
	}
}
