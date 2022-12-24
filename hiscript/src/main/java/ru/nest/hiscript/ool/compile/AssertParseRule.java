package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class AssertParseRule extends ParseRule<NodeAssert> {
	private final static AssertParseRule instance = new AssertParseRule();

	public static AssertParseRule getInstance() {
		return instance;
	}

	private AssertParseRule() {
	}

	@Override
	public NodeAssert visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.ASSERT, tokenizer) != null) {
			NodeExpression condition = expectExpression(tokenizer, ctx);
			// TODO check condition.getExpressionType() on boolean type

			NodeExpression message = null;
			if (visitSymbol(tokenizer, Symbols.COLON) != -1) {
				message = expectExpression(tokenizer, ctx);
			}
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeAssert(condition, message);
		}
		return null;
	}
}
