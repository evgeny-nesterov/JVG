package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiClass;
import ru.nest.hiscript.ool.model.HiCompiler;
import ru.nest.hiscript.ool.model.HiMethod;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.ModifiersIF;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
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
	public Node visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
		tokenizer.nextToken();

		boolean createMainMethod = false;
		if (ctx == null) {
			ctx = new CompileContext(compiler, null, HiClass.CLASS_TYPE_TOP);
			if (wrapped) {
				ctx.clazz = new HiClass(null, null, HiClass.ROOT_CLASS_NAME, HiClass.CLASS_TYPE_TOP);
				createMainMethod = true;
			}
		}

		NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, ctx);

		Node node;
		if (createMainMethod) {
			HiClass clazz = ctx.clazz;
			clazz.methods = new HiMethod[1];
			clazz.methods[0] = new HiMethod(clazz, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, null, body, body.getToken());

			node = new NodeMainWrapper(body);
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
