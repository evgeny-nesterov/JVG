package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeAnnotation;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

// TODO delete (unused)
public class ArgumentParseRule extends ParseRule<NodeArgument> {
	private final static ArgumentParseRule instance = new ArgumentParseRule();

	public static ArgumentParseRule getInstance() {
		return instance;
	}

	private ArgumentParseRule() {
	}

	@Override
	public NodeArgument visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		tokenizer.start();

		NodeAnnotation[] annotations = AnnotationParseRule.getInstance().visitAnnotations(tokenizer, ctx);
		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new ParseException("variable name is expected", tokenizer.currentToken());
			}

			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, FINAL);

			int addDimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, addDimension);
			return new NodeArgument(type, name, modifiers, annotations);
		}

		tokenizer.rollback();
		return null;
	}
}