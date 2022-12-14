package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeIf;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class IfParseRule extends ParseRule<NodeIf> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	@Override
	public NodeIf visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.IF, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			NodeExpression condition = expectCondition(tokenizer, properties);
			Node body = expectBody(tokenizer, properties);
			NodeIf elseIfNode = visitNext(tokenizer, properties);

			NodeIf ifNode = new NodeIf(condition, body, elseIfNode);
			ifNode.setToken(tokenizer.getBlockToken(startToken));
			return ifNode;
		}
		return null;
	}

	public NodeIf visitNext(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.ELSE, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();
			if (visitWord(Words.IF, tokenizer) != null) {
				NodeExpression condition = expectCondition(tokenizer, properties);
				Node body = expectBody(tokenizer, properties);
				NodeIf elseIfNode = visitNext(tokenizer, properties);

				NodeIf ifNode = new NodeIf(condition, body, elseIfNode);
				ifNode.setToken(tokenizer.getBlockToken(startToken));
				return ifNode;
			} else {
				Node body = expectBody(tokenizer, properties);

				NodeIf ifNode = new NodeIf(null, body, null);
				ifNode.setToken(tokenizer.getBlockToken(startToken));
				return ifNode;
			}
		}
		return null;
	}
}
