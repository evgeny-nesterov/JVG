package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Modifiers;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeArgument;
import ru.nest.hiscript.tokenizer.Symbols;
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
	public NodeArgument visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		Modifiers modifiers = visitModifiers(tokenizer);
		Type type = visitType(tokenizer, true);
		if (type != null) {
			if (visitSymbol(tokenizer, Symbols.TRIPLEPOINTS) != -1) {
				type = Type.getVarargsType(type);
			}

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new ParseException("variable name is expected", tokenizer.currentToken());
			}

			tokenizer.commit();
			checkModifiers(tokenizer, modifiers, FINAL);

			int addDimension = visitDimension(tokenizer);
			type = Type.getArrayType(type, addDimension);
			return new NodeArgument(type, name, modifiers);
		}

		tokenizer.rollback();
		return null;
	}
}
