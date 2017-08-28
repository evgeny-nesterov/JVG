package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeInt extends Node {
	private final static String name = "int";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeInt(int value) {
		super(name, TYPE_INT);
		this.value = value;
	}

	private int value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.intNumber = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeInt(value);
	}

	public static NodeInt decode(DecodeContext os) throws IOException {
		return new NodeInt(os.readInt());
	}
}
