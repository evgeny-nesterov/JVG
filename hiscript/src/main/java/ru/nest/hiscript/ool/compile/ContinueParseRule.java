package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeContinue;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ContinueParseRule extends ParseRule<NodeContinue> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	@Override
	public NodeContinue visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.CONTINUE, tokenizer) != null) {
			String label = visitWord(Words.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeContinue(label);
		}
		return null;
	}
}
