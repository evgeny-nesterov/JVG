package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.tokenizer.Symbols;
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
	public NodeReturn visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.RETURN, tokenizer) != null) {
			NodeExpression value = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeReturn(value);
		}
		return null;
	}
}
