package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeMainWrapper extends Node {
	public NodeMainWrapper(NodeBlock body, HiClass rootClass) {
		super("main", MAIN_WRAPPER);
		this.body = body;
		this.rootClass = getRootClass(body);
	}

	private NodeBlock body;

	private HiClass rootClass;

	public HiClass getRootClass(NodeBlock body) {
		if (rootClass == null) {
			rootClass = new HiClass(null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP);
		}
		if (rootClass.methods == null) {
			rootClass.methods = new HiMethod[1];
			rootClass.methods[0] = new HiMethod(rootClass, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, null, body, body.getToken());
		}
		return rootClass;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (ctx == null) {
			ctx = new CompileClassContext(validationInfo.getCompiler(), null, HiClass.CLASS_TYPE_TOP);
		}
		return body.validate(validationInfo, ctx);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		ctx.enterMethod(rootClass.methods[0], null);
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

	public static NodeMainWrapper decode(DecodeContext os) throws IOException {
		return new NodeMainWrapper(NodeBlock.decode(os), null);
	}
}
