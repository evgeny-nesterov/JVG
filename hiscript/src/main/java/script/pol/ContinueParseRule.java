package script.pol;

import script.ParseException;
import script.pol.model.ContinueNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ContinueParseRule extends ParseRule<ContinueNode> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	public ContinueNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.CONTINUE, tokenizer) != null) {
			String mark = visitWord(WordToken.NOT_SERVICE, tokenizer);
			return new ContinueNode(mark);
		}
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.CONTINUE, tokenizer, handler) != null) {
			visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
			return true;
		}

		return false;
	}
}
