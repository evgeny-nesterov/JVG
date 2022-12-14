package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeWhile;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class WhileParseRule extends ParseRule<NodeWhile> {
	private final static WhileParseRule instance = new WhileParseRule();

	public static WhileParseRule getInstance() {
		return instance;
	}

	private WhileParseRule() {
	}

	@Override
	public NodeWhile visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.WHILE, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			NodeExpression condition = expectCondition(tokenizer, properties);
			Node body = expectBody(tokenizer, properties);

			NodeWhile node = new NodeWhile(condition, body);
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}
		return null;
	}
}
