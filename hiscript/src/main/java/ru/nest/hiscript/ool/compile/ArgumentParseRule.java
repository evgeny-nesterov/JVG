package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

// TODO remove? (unused)
@Deprecated
public class ArgumentParseRule extends ParseRule<NodeArgument> {
	private final static ArgumentParseRule instance = new ArgumentParseRule();

	public static ArgumentParseRule getInstance() {
		return instance;
	}

	private ArgumentParseRule() {
	}

	@Override
	public NodeArgument visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Type type = visitType(tokenizer, true, ctx.getEnv());
		if (type != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				tokenizer.error("variable name is expected");
			}

			tokenizer.commit();
			checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), FINAL);

			int addDimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, addDimension, ctx.getEnv());
			return new NodeArgument(type, name, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
		}

		tokenizer.rollback();
		return null;
	}
}
