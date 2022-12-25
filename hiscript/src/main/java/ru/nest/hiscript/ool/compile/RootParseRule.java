package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeMainWrapper;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class RootParseRule extends ParseRule<HiNode> {
	private boolean wrapped;

	private HiCompiler compiler;

	private CompileClassContext ctx;

	private HiClass rootClass;

	private NodeMainWrapper mainWrapperNode;

	private boolean outerContext;

	public RootParseRule(HiCompiler compiler, boolean wrapped, boolean outerContext) {
		this.wrapped = wrapped;
		this.compiler = compiler;
		this.outerContext = outerContext;
	}

	@Override
	public HiNode visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		tokenizer.nextToken();

		boolean createMainMethod = false;
		if (ctx == null && this.ctx != null) {
			ctx = this.ctx;
			createMainMethod = wrapped;
		}
		if (ctx == null) {
			ctx = new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);

			rootClass = compiler.getClassLoader().getClass(HiClass.ROOT_CLASS_NAME);
			if (rootClass == null) {
				rootClass = new HiClass(compiler.getClassLoader(), null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP, ctx);
			}
			ctx.clazz = rootClass;

			createMainMethod = wrapped;
		}
		this.ctx = ctx;

		NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, ctx);
		if (outerContext && body != null) {
			body.setEnterType(RuntimeContext.SAME);
		}

		HiNode node;
		if (createMainMethod) {
			if (mainWrapperNode == null) {
				mainWrapperNode = new NodeMainWrapper(ctx.getClassLoader(), body, rootClass);
			} else {
				mainWrapperNode.setBody(body);
			}
			node = mainWrapperNode;
		} else {
			if (body != null) {
				body.setEnterType(RuntimeContext.START);
			}
			node = body;
		}

		skipComments(tokenizer);
		if (tokenizer.hasNext()) {
			tokenizer.error("unexpected token");
		}
		return node;
	}
}
