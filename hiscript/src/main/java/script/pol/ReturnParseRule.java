package script.pol;

import script.ParseException;
import script.pol.model.Node;
import script.pol.model.ReturnNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class ReturnParseRule extends ParseRule<ReturnNode> {
	private final static ReturnParseRule instance = new ReturnParseRule();

	public static ReturnParseRule getInstance() {
		return instance;
	}

	private ReturnParseRule() {
	}

	@Override
	public ReturnNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.RETURN, tokenizer) != null) {
			Node returnValue = ExpressionParseRule.getInstance().visit(tokenizer);
			return new ReturnNode(returnValue);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.RETURN, tokenizer, handler) != null) {
			ExpressionParseRule.getInstance().visit(tokenizer, handler);
			return true;
		}

		return false;
	}
}
