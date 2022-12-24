package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.TryCatchNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class TryCatchParseRule extends ParseRule<TryCatchNode> {
	private final static TryCatchParseRule instance = new TryCatchParseRule();

	public static TryCatchParseRule getInstance() {
		return instance;
	}

	private TryCatchParseRule() {
	}

	@Override
	public TryCatchNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.TRY, tokenizer) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer);
			BlockNode tryBody = BlockParseRule.getInstance().visit(tokenizer);
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer);

			BlockNode catchBody = null;
			String errorVariableName = null;
			boolean thereIsCatch = false;
			if (visitWord(Words.CATCH, tokenizer) != null) {
				thereIsCatch = true;

				expectSymbol(Symbols.PARENTHESES_LEFT, tokenizer);
				if (visitWord(Words.STRING, tokenizer) == null) {
					throw new HiScriptParseException("'string' expected", tokenizer.currentToken());
				}
				errorVariableName = visitWord(Words.NOT_SERVICE, tokenizer);
				if (errorVariableName == null) {
					throw new HiScriptParseException("variable expected", tokenizer.currentToken());
				}
				expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer);

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
				throw new HiScriptParseException("'catch' or 'finally' expected", tokenizer.currentToken());
			}

			return new TryCatchNode(tryBody, catchBody, finallyBody, errorVariableName);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.TRY, tokenizer, handler) != null) {
			expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);

			String errorVariableName;
			boolean thereIsCatch = false;
			if (visitWord(Words.CATCH, tokenizer, handler) != null) {
				thereIsCatch = true;

				expectSymbol(Symbols.PARENTHESES_LEFT, tokenizer, handler);
				if (visitWord(Words.STRING, tokenizer, handler) == null) {
					errorOccurred(tokenizer, handler, "'string' expected");
				}
				errorVariableName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
				if (errorVariableName == null) {
					errorOccurred(tokenizer, handler, "variable expected");
				}
				expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer, handler);

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
				errorOccurred(tokenizer, handler, "'catch' or 'finally' expected");
			}

			return true;
		}

		return false;
	}
}
