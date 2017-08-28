package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeFloat extends Node {
	private final static String name = "float";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeFloat(float value) {
		super(name, TYPE_FLOAT);
		this.value = value;
	}

	private float value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.floatNumber = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeFloat(value);
	}

	public static NodeFloat decode(DecodeContext os) throws IOException {
		return new NodeFloat(os.readFloat());
	}
}
