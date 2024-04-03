package ru.nest.hiscript.ool.model.nodes;

import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiClassLoader;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.validation.ValidationInfo;

import java.io.IOException;

public class NodeMainWrapper extends HiNode {
	public NodeMainWrapper(HiClassLoader classLoader, NodeBlock body, HiClass rootClass) {
		super("main", TYPE_MAIN_WRAPPER);
		this.classLoader = classLoader;
		this.body = body;
		this.rootClass = rootClass;
		this.rootClass = getRootClass(body);
	}

	private final HiClassLoader classLoader;

	private CompileClassContext ctx;

	private NodeBlock body;

	private HiClass rootClass;

	private HiMethod mainMethod;

	private HiClass getRootClass(NodeBlock body) {
		if (rootClass == null) {
			rootClass = new HiClass(classLoader, null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP, null);
		}
		if (rootClass.methods == null) {
			rootClass.methods = new HiMethod[1];
			mainMethod = new HiMethod(rootClass, null, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, null, body);
			mainMethod.setToken(body.getToken());
			rootClass.methods[0] = mainMethod;
		}
		return rootClass;
	}

	public void setBody(NodeBlock body) {
		this.body = body;
		mainMethod.body = body;
	}

	@Override
	public boolean validate(ValidationInfo validationInfo, CompileClassContext ctx) {
		if (ctx == null) {
			if (this.ctx != null) {
				ctx = this.ctx;
			} else {
				ctx = new CompileClassContext(validationInfo.getCompiler(), null, HiClass.CLASS_TYPE_TOP);
			}
		}
		this.ctx = ctx;
		return body != null && body.validate(validationInfo, ctx);
	}

	@Override
	public void execute(RuntimeContext ctx) {
		try {
			if (ctx.level == null) {
				ctx.enterMethod(rootClass.methods[0], null);
			}
			rootClass.methods[0].invoke(ctx, rootClass, null, null);
		} finally {
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
