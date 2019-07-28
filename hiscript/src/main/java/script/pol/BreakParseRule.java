package script.pol;

import script.ParseException;
import script.pol.model.BreakNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class BreakParseRule extends ParseRule<BreakNode> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	@Override
	public BreakNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
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
