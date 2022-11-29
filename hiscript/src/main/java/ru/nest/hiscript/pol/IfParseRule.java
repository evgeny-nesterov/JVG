package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.IfNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class IfParseRule extends ParseRule<IfNode> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	@Override
	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.IF, tokenizer) != null) {
			expectSymbol(Symbols.PARENTHESES_LEFT, tokenizer);

			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) {
				throw new ParseException("statement is expected", tokenizer.currentToken());
			}

			IfNode ifNode = new IfNode(condition, body);

			IfNode elseIfNode;
			while ((elseIfNode = ElseIfParseRule.getInstance().visit(tokenizer)) != null) {
				ifNode.addElseIf(elseIfNode);
			}

			IfNode elseNode = ElseParseRule.getInstance().visit(tokenizer);
			if (elseNode != null) {
				ifNode.addElseIf(elseNode);
			}

			return ifNode;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.IF, tokenizer, handler) != null) {
			expectSymbol(Symbols.PARENTHESES_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression is expected");
			}
			expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "statement is expected");
			}

			while (ElseIfParseRule.getInstance().visit(tokenizer, handler)) {
				;
			}

			ElseParseRule.getInstance().visit(tokenizer, handler);

			return true;
		}

		return false;
	}
}
