package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class TypeParseRule extends ParseRule<NodeType> {
	private final static TypeParseRule instance = new TypeParseRule();

	public static TypeParseRule getInstance() {
		return instance;
	}

	private TypeParseRule() {
	}

	@Override
	public NodeType visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		Type type = visitType(tokenizer, true);
		if (type != null) {
			return new NodeType(type);
		}

		return null;
	}
}
