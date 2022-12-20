package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
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
	public NodeWhile visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.WHILE, tokenizer) != null) {
			NodeExpression condition = expectCondition(tokenizer, ctx);
			HiNode body = expectBody(tokenizer, ctx);
			return new NodeWhile(condition, body);
		}
		return null;
	}
}
