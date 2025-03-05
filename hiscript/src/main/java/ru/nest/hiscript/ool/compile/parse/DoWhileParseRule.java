package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeDoWhile;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.tokenizer.SymbolType;
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
	public NodeDoWhile visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.DO, tokenizer) != null) {
			expectSymbol(tokenizer, SymbolType.BRACES_LEFT);
			NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, ctx);
			expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);
			expectWord(Words.WHILE, tokenizer);
			NodeExpression condition = expectCondition(tokenizer, ctx);
			return new NodeDoWhile(body, condition);
		}
		return null;
	}
}
