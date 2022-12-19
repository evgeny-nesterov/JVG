package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compiler.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeMainWrapper extends Node {
	public NodeMainWrapper(HiClassLoader classLoader, NodeBlock body, HiClass rootClass) {
		super("main", MAIN_WRAPPER);
		this.classLoader = classLoader;
		this.body = body;
		this.rootClass = rootClass;
		this.rootClass = getRootClass(body);
	}

	private HiClassLoader classLoader;

	private NodeBlock body;

	private HiClass rootClass;

	private HiClass getRootClass(NodeBlock body) {
		if (rootClass == null) {
			rootClass = new HiClass(classLoader, null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP, null);
		}
		if (rootClass.methods == null) {
			rootClass.methods = new HiMethod[1];
			HiMethod method = new HiMethod(rootClass, null, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, null, body);
			method.token = body.getToken();
			rootClass.methods[0] = method;
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
		return new NodeMainWrapper(os.getClassLoader(), NodeBlock.decode(os), null);
	}
}
