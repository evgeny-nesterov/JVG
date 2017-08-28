package script.pol;

import script.ParseException;
import script.pol.model.DoWhileNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class DoWhileParseRule extends ParseRule<DoWhileNode> {
	private final static DoWhileParseRule instance = new DoWhileParseRule();

	public static DoWhileParseRule getInstance() {
		return instance;
	}

	private DoWhileParseRule() {
	}

	public DoWhileNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.DO, tokenizer) != null) {
			expectSymbol(SymbolToken.BRACES_LEFT, tokenizer);
			Node body = BlockParseRule.getInstance().visit(tokenizer); // may be
			// empty;
			expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer);

			if (visitWord(WordToken.WHILE, tokenizer) == null) {
				throw new ParseException("while expected", tokenizer.currentToken());
			}

			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);

			return new DoWhileNode(condition, body);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.DO, tokenizer, handler) != null) {
			expectSymbol(SymbolToken.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer, handler);

			if (visitWord(WordToken.WHILE, tokenizer, handler) == null) {
				errorOccured(tokenizer, handler, "while expected");
			}

			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);
			expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);

			return true;
		}

		return false;
	}
}
