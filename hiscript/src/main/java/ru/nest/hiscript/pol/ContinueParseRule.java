package ru.nest.hiscript.pol;

import ru.nest.hiscript.pol.model.ContinueNode;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class ContinueParseRule extends ParseRule<ContinueNode> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	@Override
	public ContinueNode visit(Tokenizer tokenizer) throws TokenizerException {
		if (visitWord(WordType.CONTINUE, tokenizer) != null) {
			String mark = visitWord(WordType.NOT_SERVICE, tokenizer);
			return new ContinueNode(mark);
		}
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordType.CONTINUE, tokenizer, handler) != null) {
			visitWord(WordType.NOT_SERVICE, tokenizer, handler);
			return true;
		}

		return false;
	}
}
