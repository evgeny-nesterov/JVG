package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.AssignmentsNode;
import ru.nest.hiscript.pol.model.DeclarationsNode;
import ru.nest.hiscript.pol.model.ForNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class ForParseRule extends ParseRule<ForNode> {
	private final static ForParseRule instance = new ForParseRule();

	public static ForParseRule getInstance() {
		return instance;
	}

	private ForParseRule() {
	}

	@Override
	public ForNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.FOR, tokenizer) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);

			DeclarationsNode initialization1 = DeclarationsParseRule.getInstance().visit(tokenizer);
			AssignmentsNode initialization2 = null;
			if (initialization1 == null) {
				initialization2 = AssignmentsParseRule.getInstance().visit(tokenizer);
			}

			expectSymbol(SymbolType.SEMICOLON, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			expectSymbol(SymbolType.SEMICOLON, tokenizer);
			Node assignments = AssignmentsParseRule.getInstance().visit(tokenizer);
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) {
				throw new HiScriptParseException("statement is expected", tokenizer.currentToken());
			}

			if (initialization1 != null) {
				return new ForNode(initialization1, condition, assignments, body);
			} else if (initialization2 != null) {
				return new ForNode(initialization2, condition, assignments, body);
			} else {
				return new ForNode(condition, assignments, body);
			}
		}
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordType.FOR, tokenizer, handler) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);

			if (!DeclarationsParseRule.getInstance().visit(tokenizer, handler)) {
				AssignmentsParseRule.getInstance().visit(tokenizer, handler);
			}

			expectSymbol(SymbolType.SEMICOLON, tokenizer, handler);
			ExpressionParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(SymbolType.SEMICOLON, tokenizer, handler);
			AssignmentsParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "statement is expected");
			}

			return true;
		}

		return false;
	}
}
