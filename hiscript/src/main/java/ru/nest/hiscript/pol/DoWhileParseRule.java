package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.DoWhileNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class DoWhileParseRule extends ParseRule<DoWhileNode> {
	private final static DoWhileParseRule instance = new DoWhileParseRule();

	public static DoWhileParseRule getInstance() {
		return instance;
	}

	private DoWhileParseRule() {
	}

	@Override
	public DoWhileNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.DO, tokenizer) != null) {
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
			Node body = BlockParseRule.getInstance().visit(tokenizer); // may be
			// empty;
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);

			if (visitWord(WordType.WHILE, tokenizer) == null) {
				throw new HiScriptParseException("while expected", tokenizer.currentToken());
			}

			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new HiScriptParseException("expression expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);
			expectSymbol(SymbolType.SEMICOLON, tokenizer);

			return new DoWhileNode(condition, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordType.DO, tokenizer, handler) != null) {
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);

			if (visitWord(WordType.WHILE, tokenizer, handler) == null) {
				errorOccurred(tokenizer, handler, "while expected");
			}

			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);
			expectSymbol(SymbolType.SEMICOLON, tokenizer, handler);

			return true;
		}

		return false;
	}
}
