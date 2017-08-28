package script.pol;

import script.ParseException;
import script.pol.model.BlockNode;
import script.pol.model.CaseNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class CaseParseRule extends ParseRule<CaseNode> {
	private final static CaseParseRule instance = new CaseParseRule();

	public static CaseParseRule getInstance() {
		return instance;
	}

	private CaseParseRule() {
	}

	public CaseNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.CASE, tokenizer) != null) {
			Node value;
			if ((value = ExpressionParseRule.getInstance().visit(tokenizer)) == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.COLON, tokenizer);
			BlockNode body = BlockParseRule.getInstance().visit(tokenizer);
			return new CaseNode(value, body);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.CASE, tokenizer, handler) != null) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(SymbolToken.COLON, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			return true;
		}

		return false;
	}
}
