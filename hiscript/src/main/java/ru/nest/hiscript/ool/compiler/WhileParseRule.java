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
	public NodeWhile visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		if (visitWord(Words.WHILE, tokenizer) != null) {
			Token startToken = startToken(tokenizer);
			NodeExpression condition = expectCondition(tokenizer, ctx);
			Node body = expectBody(tokenizer, ctx);

			NodeWhile node = new NodeWhile(condition, body);
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}
		return null;
	}
}
