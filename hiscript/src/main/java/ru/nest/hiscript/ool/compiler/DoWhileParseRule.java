package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeDoWhile;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class DoWhileParseRule extends ParseRule<NodeDoWhile> {
	private final static DoWhileParseRule instance = new DoWhileParseRule();

	public static DoWhileParseRule getInstance() {
		return instance;
	}

	private DoWhileParseRule() {
	}

	@Override
	public NodeDoWhile visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.DO, tokenizer) != null) {
			Token startToken = tokenizer.currentToken();

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, properties);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			expectWord(Words.WHILE, tokenizer);
			NodeExpression condition = expectCondition(tokenizer, properties);

			NodeDoWhile node = new NodeDoWhile(body, condition);
			node.setToken(tokenizer.getBlockToken(startToken));
			return node;
		}
		return null;
	}
}
