package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;
import script.ool.model.Value;

public class NodeType extends Node {
	public NodeType(Type type) {
		super("type", TYPE_TYPE);
		this.type = type;
	}

	private Type type;

	public Type getType() {
		return type;
	}

	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.TYPE;
		ctx.value.variableType = type;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeType(type);
	}

	public static NodeType decode(DecodeContext os) throws IOException {
		return new NodeType(os.readType());
	}
}
