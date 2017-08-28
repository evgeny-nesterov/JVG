package script.pol;

import script.ParseException;
import script.pol.model.IfNode;
import script.pol.model.Node;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ElseParseRule extends ParseRule<IfNode> {
	private final static ElseParseRule instance = new ElseParseRule();

	public static ElseParseRule getInstance() {
		return instance;
	}

	private ElseParseRule() {
	}

	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.ELSE, tokenizer) != null) {
			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) {
				throw new ParseException("Statement is expected", tokenizer.currentToken());
			}

			return new IfNode(null, body);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.ELSE, tokenizer, handler) != null) {
			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "Statement is expected");
			}

			return true;
		}

		return false;
	}
}
