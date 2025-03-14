package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.AnnotatedModifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.TypeVarargs;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

import static ru.nest.hiscript.tokenizer.WordType.*;

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

		AnnotatedModifiers annotatedModifiers = visitAnnotatedModifiers(tokenizer, ctx, false);
		Type type = visitType(tokenizer, true, ctx.getEnv());
		if (type != null) {
			boolean vararg = visitSymbol(tokenizer, SymbolType.TRIPLE_POINTS) != null;
			String name = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			if (name != null) {
				tokenizer.commit();
				checkModifiers(tokenizer, annotatedModifiers.getModifiers(), annotatedModifiers.getToken(), FINAL);

				int addDimension = visitDimension(tokenizer);
				type = Type.getArrayType(type, addDimension, ctx.getEnv());

				TypeArgumentIF typeArgument;
				if (vararg) {
					type = Type.getArrayType(type, 1, ctx.getEnv());
					typeArgument = new TypeVarargs(type);
				} else {
					typeArgument = type;
				}
				return new NodeArgument(typeArgument, name, annotatedModifiers.getModifiers(), annotatedModifiers.getAnnotations());
			}
		}

		tokenizer.rollback();
		return null;
	}
}
