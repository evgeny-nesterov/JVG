package script.pol;

import script.ParseException;
import script.pol.model.BreakNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class BreakParseRule extends ParseRule<BreakNode> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	public BreakNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.BREAK, tokenizer) != null) {
			String mark = visitWord(WordToken.NOT_SERVICE, tokenizer);
			return new BreakNode(mark);
		}
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.BREAK, tokenizer, handler) != null) {
			visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
			return true;
		}

		return false;
	}
}
