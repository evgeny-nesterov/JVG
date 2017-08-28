package script.pol;

import script.ParseException;
import script.pol.model.BlockNode;
import script.pol.model.TryCatchNode;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.Words;

public class TryCatchParseRule extends ParseRule<TryCatchNode> {
	private final static TryCatchParseRule instance = new TryCatchParseRule();

	public static TryCatchParseRule getInstance() {
		return instance;
	}

	private TryCatchParseRule() {
	}

	public TryCatchNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.TRY, tokenizer) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer);
			BlockNode tryBody = BlockParseRule.getInstance().visit(tokenizer);
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer);

			BlockNode catchBody = null;
			String errorVariableName = null;
			boolean thereIsCatch = false;
			if (visitWord(Words.CATCH, tokenizer) != null) {
				thereIsCatch = true;

				expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer);
				if (visitWord(Words.STRING, tokenizer) == null) {
					throw new ParseException("'string' expected", tokenizer.currentToken());
				}
				errorVariableName = visitWord(Words.NOT_SERVICE, tokenizer);
				if (errorVariableName == null) {
					throw new ParseException("variable expected", tokenizer.currentToken());
				}
				expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);

				expectSymbol(Symbols.BRACES_LEFT, tokenizer);
				catchBody = BlockParseRule.getInstance().visit(tokenizer);
				expectSymbol(Symbols.BRACES_RIGHT, tokenizer);
			}

			BlockNode finallyBody = null;
			boolean thereIsFinally = false;
			if (visitWord(Words.FINALLY, tokenizer) != null) {
				thereIsFinally = true;
				expectSymbol(Symbols.BRACES_LEFT, tokenizer);
				finallyBody = BlockParseRule.getInstance().visit(tokenizer);
				expectSymbol(Symbols.BRACES_RIGHT, tokenizer);
			}

			if (!thereIsCatch && !thereIsFinally) {
				throw new ParseException("'catch' or 'finally' expected", tokenizer.currentToken());
			}

			return new TryCatchNode(tryBody, catchBody, finallyBody, errorVariableName);
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.TRY, tokenizer, handler) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);

			String errorVariableName = null;
			boolean thereIsCatch = false;
			if (visitWord(Words.CATCH, tokenizer, handler) != null) {
				thereIsCatch = true;

				expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer, handler);
				if (visitWord(Words.STRING, tokenizer, handler) == null) {
					errorOccured(tokenizer, handler, "'string' expected");
				}
				errorVariableName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
				if (errorVariableName == null) {
					errorOccured(tokenizer, handler, "variable expected");
				}
				expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);

				expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
				BlockParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);
			}

			boolean thereIsFinally = false;
			if (visitWord(Words.FINALLY, tokenizer, handler) != null) {
				thereIsFinally = true;
				expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
				BlockParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);
			}

			if (!thereIsCatch && !thereIsFinally) {
				errorOccured(tokenizer, handler, "'catch' or 'finally' expected");
			}

			return true;
		}

		return false;
	}
}
