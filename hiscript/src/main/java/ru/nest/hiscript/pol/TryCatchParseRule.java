package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.TryCatchNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class TryCatchParseRule extends ParseRule<TryCatchNode> {
	private final static TryCatchParseRule instance = new TryCatchParseRule();

	public static TryCatchParseRule getInstance() {
		return instance;
	}

	private TryCatchParseRule() {
	}

	@Override
	public TryCatchNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.TRY, tokenizer) != null) {
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
			BlockNode tryBody = BlockParseRule.getInstance().visit(tokenizer);
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);

			BlockNode catchBody = null;
			String errorVariableName = null;
			boolean thereIsCatch = false;
			if (visitWord(WordType.CATCH, tokenizer) != null) {
				thereIsCatch = true;

				expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);
				if (visitWord(WordType.STRING, tokenizer) == null) {
					throw new HiScriptParseException("'string' expected", tokenizer.currentToken());
				}
				errorVariableName = visitWord(WordType.NOT_SERVICE, tokenizer);
				if (errorVariableName == null) {
					throw new HiScriptParseException("variable expected", tokenizer.currentToken());
				}
				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

				expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
				catchBody = BlockParseRule.getInstance().visit(tokenizer);
				expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);
			}

			BlockNode finallyBody = null;
			boolean thereIsFinally = false;
			if (visitWord(WordType.FINALLY, tokenizer) != null) {
				thereIsFinally = true;
				expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
				finallyBody = BlockParseRule.getInstance().visit(tokenizer);
				expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);
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
		if (visitWord(WordType.TRY, tokenizer, handler) != null) {
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);

			String errorVariableName;
			boolean thereIsCatch = false;
			if (visitWord(WordType.CATCH, tokenizer, handler) != null) {
				thereIsCatch = true;

				expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);
				if (visitWord(WordType.STRING, tokenizer, handler) == null) {
					errorOccurred(tokenizer, handler, "'string' expected");
				}
				errorVariableName = visitWord(WordType.NOT_SERVICE, tokenizer, handler);
				if (errorVariableName == null) {
					errorOccurred(tokenizer, handler, "variable expected");
				}
				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

				expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
				BlockParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);
			}

			boolean thereIsFinally = false;
			if (visitWord(WordType.FINALLY, tokenizer, handler) != null) {
				thereIsFinally = true;
				expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
				BlockParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);
			}

			if (!thereIsCatch && !thereIsFinally) {
				errorOccurred(tokenizer, handler, "'catch' or 'finally' expected");
			}
			return true;
		}
		return false;
	}
}
