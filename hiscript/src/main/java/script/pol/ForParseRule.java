package script.pol;

import script.ParseException;
import script.pol.model.AssignmentsNode;
import script.pol.model.DeclarationsNode;
import script.pol.model.ForNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class ForParseRule extends ParseRule<ForNode> {
	private final static ForParseRule instance = new ForParseRule();

	public static ForParseRule getInstance() {
		return instance;
	}

	private ForParseRule() {
	}

	@Override
	public ForNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.FOR, tokenizer) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer);

			DeclarationsNode initialization1 = DeclarationsParseRule.getInstance().visit(tokenizer);
			AssignmentsNode initialization2 = null;
			if (initialization1 == null) {
				initialization2 = AssignmentsParseRule.getInstance().visit(tokenizer);
			}

			expectSymbol(Symbols.SEMICOLON, tokenizer);
			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			expectSymbol(Symbols.SEMICOLON, tokenizer);
			Node asignments = AssignmentsParseRule.getInstance().visit(tokenizer);
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);

			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) {
				throw new ParseException("statement is expected", tokenizer.currentToken());
			}

			if (initialization1 != null) {
				return new ForNode(initialization1, condition, asignments, body);
			} else if (initialization2 != null) {
				return new ForNode(initialization2, condition, asignments, body);
			} else {
				return new ForNode(condition, asignments, body);
			}
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.FOR, tokenizer, handler) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer, handler);

			if (!DeclarationsParseRule.getInstance().visit(tokenizer, handler)) {
				AssignmentsParseRule.getInstance().visit(tokenizer, handler);
			}

			expectSymbol(Symbols.SEMICOLON, tokenizer, handler);
			ExpressionParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(Symbols.SEMICOLON, tokenizer, handler);
			AssignmentsParseRule.getInstance().visit(tokenizer, handler);
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "statement is expected");
			}

			return true;
		}

		return false;
	}
}
