package script.pol;

import script.ParseException;
import script.pol.model.IfNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ElseIfParseRule extends ParseRule<IfNode> {
	private final static ElseIfParseRule instance = new ElseIfParseRule();

	public static ElseIfParseRule getInstance() {
		return instance;
	}

	private ElseIfParseRule() {
	}

	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();
		if (visitWord(WordToken.ELSE, tokenizer) != null) {
			if (visitWord(WordToken.IF, tokenizer) != null) {
				tokenizer.commit();

				expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer);
				Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
				if (condition == null) {
					throw new ParseException("expression is expected", tokenizer.currentToken());
				}
				expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);

				Node body = StatementParseRule.getInstance().visit(tokenizer);
				if (body == null) {
					throw new ParseException("statement is expected", tokenizer.currentToken());
				}

				return new IfNode(condition, body);
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();
		if (visitWord(WordToken.ELSE, tokenizer, handler) != null) {
			if (visitWord(WordToken.IF, tokenizer, handler) != null) {
				tokenizer.commit();

				expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer, handler);
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "expression is expected");
				}
				expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);

				if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "statement is expected");
				}

				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
