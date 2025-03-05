package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.WhileNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class WhileParseRule extends ParseRule<WhileNode> {
	private final static WhileParseRule instance = new WhileParseRule();

	public static WhileParseRule getInstance() {
		return instance;
	}

	private WhileParseRule() {
	}

	@Override
	public WhileNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.WHILE, tokenizer) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new HiScriptParseException("expression expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) // may be ';'
			{
				throw new HiScriptParseException("statement is expected", tokenizer.currentToken());
			}

			return new WhileNode(condition, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordType.WHILE, tokenizer, handler) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "statement expected");
			}

			return true;
		}

		return false;
	}
}
