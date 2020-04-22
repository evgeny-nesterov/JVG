package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Clazz;
import ru.nest.hiscript.ool.model.Method;
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
	private final static RootParseRule instance = new RootParseRule(false);

	private final static RootParseRule instanceWrapped = new RootParseRule(true);

	public static RootParseRule getInstance() {
		return instance;
	}

	public static RootParseRule getInstanceWrapped() {
		return instanceWrapped;
	}

	private boolean wrapped;

	private RootParseRule(boolean wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public Node visit(Tokenizer tokenizer, CompileContext ctx) throws TokenizerException, ParseException {
		tokenizer.nextToken();

		boolean createMainMethod = false;
		if (ctx == null) {
			ctx = new CompileContext(tokenizer, null, null, Clazz.CLASS_TYPE_TOP);
			if (wrapped) {
				ctx.clazz = new Clazz(null, null, "@root", Clazz.CLASS_TYPE_TOP);
				createMainMethod = true;
			}
		}

		NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, ctx);

		Node node;
		if (createMainMethod) {
			Clazz clazz = ctx.clazz;
			clazz.methods = new Method[1];
			clazz.methods[0] = new Method(clazz, new Modifiers(ModifiersIF.ACCESS_PUBLIC | ModifiersIF.STATIC), Type.getPrimitiveType("void"), "main", (NodeArgument[]) null, body);

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
