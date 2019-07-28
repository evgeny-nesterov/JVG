package script.ool.model.nodes;

import java.io.IOException;

import script.ool.model.Clazz;
import script.ool.model.Method;
import script.ool.model.Modifiers;
import script.ool.model.ModifiersIF;
import script.ool.model.Node;
import script.ool.model.RuntimeContext;
import script.ool.model.Type;

public class NodeMainWrapper extends Node {
	public NodeMainWrapper(NodeBlock body) {
		super("main", MAIN_WRAPPER);
		this.body = body;
	}

	private NodeBlock body;

	@Override
	public void execute(RuntimeContext ctx) {
		Clazz rootClass = new Clazz(null, null, "", Clazz.CLASS_TYPE_TOP);
		rootClass.methods = new Method[1];
		rootClass.methods[0] = new Method(rootClass, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, body);

		ctx.enterMethod(rootClass.methods[0], null, -1);
		try {
			rootClass.methods[0].invoke(ctx, rootClass, null, null);
		} finally {
			ctx.exit();
			ctx.isReturn = false;
		}
	}

	@Override
	public void code(CodeContext os) throws IOException {
		body.code(os);
	}

	public static NodeBlock decode(DecodeContext os) throws IOException {
		return NodeBlock.decode(os);
	}
}
