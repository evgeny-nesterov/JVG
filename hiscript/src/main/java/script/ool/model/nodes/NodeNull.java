package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Value;
import script.ool.model.classes.ClazzNull;

public class NodeNull extends Node {
	public final static NodeNull instance = new NodeNull();

	private NodeNull() {
		super("null", TYPE_NULL);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.value.valueType = Value.VALUE;
		ctx.value.type = ClazzNull.NULL;
		ctx.value.object = null;
		ctx.value.array = null;
	}

	@Override
	public void code(CodeContext os) throws IOException {
		super.code(os);
	}

	public static NodeNull decode(DecodeContext os) throws IOException {
		return instance;
	}
}
