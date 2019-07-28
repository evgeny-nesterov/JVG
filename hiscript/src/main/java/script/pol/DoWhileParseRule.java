package script.pol;

import script.ParseException;
import script.pol.model.DoWhileNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class DoWhileParseRule extends ParseRule<DoWhileNode> {
	private final static DoWhileParseRule instance = new DoWhileParseRule();

	public static DoWhileParseRule getInstance() {
		return instance;
	}

	private DoWhileParseRule() {
	}

	@Override
	public DoWhileNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.DO, tokenizer) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer);
			Node body = BlockParseRule.getInstance().visit(tokenizer); // may be
			// empty;
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer);

			if (visitWord(Words.WHILE, tokenizer) == null) {
				throw new ParseException("while expected", tokenizer.currentToken());
			}

			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);
			expectSymbol(Symbols.SEMICOLON, tokenizer);

			return new DoWhileNode(condition, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.DO, tokenizer, handler) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);

			if (visitWord(Words.WHILE, tokenizer, handler) == null) {
				errorOccured(tokenizer, handler, "while expected");
			}

			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);
			expectSymbol(Symbols.SEMICOLON, tokenizer, handler);

			return true;
		}

		return false;
	}
}
