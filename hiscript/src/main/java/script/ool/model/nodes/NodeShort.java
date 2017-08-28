package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeShort extends Node {
	private final static String name = "short";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeShort(short value) {
		super(name, TYPE_SHORT);
		this.value = value;
	}

	private short value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.shortNumber = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeShort(value);
	}

	public static NodeShort decode(DecodeContext os) throws IOException {
		return new NodeShort(os.readShort());
	}
}
