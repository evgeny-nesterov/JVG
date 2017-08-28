package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeLong extends Node {
	private final static String name = "long";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeLong(long value) {
		super(name, TYPE_LONG);
		this.value = value;
	}

	private long value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.longNumber = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeLong(value);
	}

	public static NodeLong decode(DecodeContext os) throws IOException {
		return new NodeLong(os.readLong());
	}
}
