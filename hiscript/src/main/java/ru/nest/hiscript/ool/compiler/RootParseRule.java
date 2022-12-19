package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeMainWrapper;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class RootParseRule extends ParseRule<Node> {
	private boolean wrapped;

	private HiCompiler compiler;

	public RootParseRule(HiCompiler compiler, boolean wrapped) {
		this.wrapped = wrapped;
		this.compiler = compiler;
	}

	@Override
	public Node visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.nextToken();

		boolean createMainMethod = false;
		HiClass rootClass = null;
		if (ctx == null) {
			rootClass = new HiClass(compiler.getClassLoader(), null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP, ctx);

			ctx = new CompileClassContext(compiler, null, HiClass.CLASS_TYPE_TOP);
			ctx.clazz = rootClass;
			createMainMethod = wrapped;
		}

		NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, ctx);

		Node node;
		if (createMainMethod) {
			NodeMainWrapper mainWrapperNode = new NodeMainWrapper(ctx.getClassLoader(), body, rootClass);
			node = mainWrapperNode;
		} else {
			if (body != null) {
				body.setEnterType(RuntimeContext.START);
			}
			node = body;
		}

		skipComments(tokenizer);
		if (tokenizer.hasNext()) {
			throw new ParseException("unexpected token", tokenizer.currentToken());
		}
		return node;
	}
}
