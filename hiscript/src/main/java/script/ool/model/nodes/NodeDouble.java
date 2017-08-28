package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;

public class NodeDouble extends Node {
	private final static String name = "double";

	private final static Clazz type = Clazz.getPrimitiveClass(name);

	public NodeDouble(double value) {
		super(name, TYPE_DOUBLE);
		this.value = value;
	}

	private double value;

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = type;
		ctx.value.doubleNumber = value;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeDouble(value);
	}

	public static NodeDouble decode(DecodeContext os) throws IOException {
		return new NodeDouble(os.readDouble());
	}
}
