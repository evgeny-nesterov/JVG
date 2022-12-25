package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.TypeVarargs;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class MethodArgumentParseRule extends ParseRule<NodeArgument> {
	private final static MethodArgumentParseRule instance = new MethodArgumentParseRule();

	public static MethodArgumentParseRule getInstance() {
		return instance;
	}

	private MethodArgumentParseRule() {
	}

	@Override
	public NodeArgument visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			boolean vararg = visitSymbol(tokenizer, Symbols.TRIPLE_POINTS) != -1;

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				tokenizer.error("variable name is expected");
			}

			tokenizer.commit();
			checkModifiers(tokenizer, annotatedModifiers.getModifiers(), FINAL);

			int addDimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, addDimension);

			TypeArgumentIF typeArgument;
			if (vararg) {
				type = Type.getArrayType(type, 1);
				typeArgument = new TypeVarargs(type);
			} else {
				typeArgument = type;
			}
			return new NodeArgument(typeArgument, name, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
		}

		tokenizer.rollback();
		return null;
	}
}
