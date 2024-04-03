package ru.nest.hiscript.pol;

import ru.nest.hiscript.pol.model.BreakNode;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class BreakParseRule extends ParseRule<BreakNode> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	@Override
	public BreakNode visit(Tokenizer tokenizer) throws TokenizerException {
		if (visitWord(Words.BREAK, tokenizer) != null) {
			String mark = visitWord(Words.NOT_SERVICE, tokenizer);
			return new BreakNode(mark);
		}
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.BREAK, tokenizer, handler) != null) {
			visitWord(Words.NOT_SERVICE, tokenizer, handler);
			return true;
		}
		return false;
	}
}
