package script.pol;

import script.ParseException;
import script.pol.model.Node;
import script.pol.model.ReturnNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ReturnParseRule extends ParseRule<ReturnNode> {
	private final static ReturnParseRule instance = new ReturnParseRule();

	public static ReturnParseRule getInstance() {
		return instance;
	}

	private ReturnParseRule() {
	}

	public ReturnNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.RETURN, tokenizer) != null) {
			Node returnValue = ExpressionParseRule.getInstance().visit(tokenizer);
			return new ReturnNode(returnValue);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.RETURN, tokenizer, handler) != null) {
			ExpressionParseRule.getInstance().visit(tokenizer, handler);
			return true;
		}

		return false;
	}
}
