package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeThrow;
import ru.nest.hiscript.tokenizer.Symbols;
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
	public NodeThrow visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.THROW, tokenizer) != null) {
			NodeExpression exception = expectExpression(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeThrow(exception);
		}
		return null;
	}
}
