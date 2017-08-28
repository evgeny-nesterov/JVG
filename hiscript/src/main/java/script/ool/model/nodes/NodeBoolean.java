package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeBoolean extends Node {
	private final static String name = "boolean";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	private final static NodeBoolean TRUE = new NodeBoolean(true);

	private final static NodeBoolean FALSE = new NodeBoolean(false);

	public static NodeBoolean getInstance(boolean value) {
		return value ? TRUE : FALSE;
	}

	private NodeBoolean(boolean value) {
		super(name, TYPE_BOOLEAN);
		this.value = value;
	}

	private boolean value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.bool = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeBoolean(value);
	}

	public static NodeBoolean decode(DecodeContext os) throws IOException {
		return getInstance(os.readBoolean());
	}
}
