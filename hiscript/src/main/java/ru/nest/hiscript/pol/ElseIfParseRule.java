package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.IfNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class ElseIfParseRule extends ParseRule<IfNode> {
	private final static ElseIfParseRule instance = new ElseIfParseRule();

	public static ElseIfParseRule getInstance() {
		return instance;
	}

	private ElseIfParseRule() {
	}

	@Override
	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		if (visitWord(WordType.ELSE, tokenizer) != null) {
			if (visitWord(WordType.IF, tokenizer) != null) {
				tokenizer.commit();

				expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);
				Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
				if (condition == null) {
					throw new HiScriptParseException("expression expected", tokenizer.currentToken());
				}
				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

				Node body = StatementParseRule.getInstance().visit(tokenizer);
				if (body == null) {
					throw new HiScriptParseException("statement is expected", tokenizer.currentToken());
				}

				return new IfNode(condition, body);
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();
		if (visitWord(WordType.ELSE, tokenizer, handler) != null) {
			if (visitWord(WordType.IF, tokenizer, handler) != null) {
				tokenizer.commit();

				expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);
				if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccurred(tokenizer, handler, "expression expected");
				}
				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

				if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccurred(tokenizer, handler, "statement is expected");
				}

				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
