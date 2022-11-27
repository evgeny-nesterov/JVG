package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.WhileNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class WhileParseRule extends ParseRule<WhileNode> {
	private final static WhileParseRule instance = new WhileParseRule();

	public static WhileParseRule getInstance() {
		return instance;
	}

	private WhileParseRule() {
	}

	@Override
	public WhileNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.WHILE, tokenizer) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) // may be ';'
			{
				throw new ParseException("statement is expected", tokenizer.currentToken());
			}

			return new WhileNode(condition, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.WHILE, tokenizer, handler) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "Expression is expected");
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "Statement is expected");
			}

			return true;
		}

		return false;
	}
}
