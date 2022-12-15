package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ReturnParseRule extends ParseRule<NodeReturn> {
	private final static ReturnParseRule instance = new ReturnParseRule();

	public static ReturnParseRule getInstance() {
		return instance;
	}

	private ReturnParseRule() {
	}

	@Override
	public NodeReturn visit(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.RETURN, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			NodeExpression value = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			NodeReturn node = new NodeReturn(value);
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}
		return null;
	}
}
