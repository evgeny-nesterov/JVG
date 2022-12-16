package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.TypeArgumentIF;
import ru.nest.hiscript.ool.model.TypeVarargs;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
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
	public NodeArgument visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		tokenizer.start();
		Token startToken = startToken(tokenizer);

		NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			boolean vararg = visitSymbol(tokenizer, Symbols.TRIPLE_POINTS) != -1;

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new ParseException("variable name is expected", tokenizer.currentToken());
			}

			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, FINAL);

			int addDimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, addDimension);

			TypeArgumentIF typeArgument;
			if (vararg) {
				type = Type.getArrayType(type, 1);
				typeArgument = new TypeVarargs(type);
			} else {
				typeArgument = type;
			}

			NodeArgument node = new NodeArgument(typeArgument, name, modifiers, annotations);
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}

		tokenizer.rollback();
		return null;
	}
}
