package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.IfNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class IfParseRule extends ParseRule<IfNode> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	@Override
	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.IF, tokenizer) != null) {
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
		if (visitWord(WordType.IF, tokenizer, handler) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

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
