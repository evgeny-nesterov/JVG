package script.pol;

import script.ParseException;
import script.pol.model.Node;
import script.pol.model.WhileNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class WhileParseRule extends ParseRule<WhileNode> {
	private final static WhileParseRule instance = new WhileParseRule();

	public static WhileParseRule getInstance() {
		return instance;
	}

	private WhileParseRule() {
	}

	public WhileNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.WHILE, tokenizer) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) // may be ';'
			{
				throw new ParseException("statement is expected", tokenizer.currentToken());
			}

			return new WhileNode(condition, body);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.WHILE, tokenizer, handler) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "Expression is expected");
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "Statement is expected");
			}

			return true;
		}

		return false;
	}
}
