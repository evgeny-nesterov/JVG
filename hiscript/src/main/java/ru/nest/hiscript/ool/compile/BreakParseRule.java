package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeBreak;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class BreakParseRule extends ParseRule<NodeBreak> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	@Override
	public NodeBreak visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.BREAK, tokenizer) != null) {
			String label = visitWord(Words.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeBreak(label);
		}
		return null;
	}
}
