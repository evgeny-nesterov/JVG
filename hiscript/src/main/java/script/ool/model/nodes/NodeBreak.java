package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Node;
import script.ool.model.RuntimeContext;

public class NodeBreak extends Node {
	public NodeBreak(String label) {
		super("break", TYPE_BREAK);
		this.label = label != null ? label.intern() : null;
	}

	private String label;

	public void execute(RuntimeContext ctx) {
		ctx.isBreak = true;
		ctx.label = label;
	}

	public void code(CodeContext os) throws IOException {
		super.code(os);
		os.writeNullableUTF(label);
	}

	public static NodeBreak decode(DecodeContext os) throws IOException {
		return new NodeBreak(os.readNullableUTF());
	}
}
