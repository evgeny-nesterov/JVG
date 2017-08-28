package script.pol;

import script.ParseException;
import script.pol.model.IfNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class IfParseRule extends ParseRule<IfNode> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.IF, tokenizer) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer);

			Node condition = ExpressionParseRule.getInstance().visit(tokenizer);
			if (condition == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);

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

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.IF, tokenizer, handler) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);

			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "statement is expected");
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
