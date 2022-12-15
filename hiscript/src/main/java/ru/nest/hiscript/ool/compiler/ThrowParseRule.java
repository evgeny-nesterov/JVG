package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeThrow;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ThrowParseRule extends ParseRule<NodeThrow> {
	private final static ThrowParseRule instance = new ThrowParseRule();

	public static ThrowParseRule getInstance() {
		return instance;
	}

	private ThrowParseRule() {
	}

	@Override
	public NodeThrow visit(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.THROW, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			NodeThrow node = new NodeThrow(expectExpression(tokenizer, properties));
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}
		return null;
	}
}
