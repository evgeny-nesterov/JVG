package script.pol;

import script.ParseException;
import script.pol.model.ArrayNode;
import script.pol.model.ExpressionNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class ArrayParseRule extends ParseRule<ArrayNode> {
	private final static ArrayParseRule instance = new ArrayParseRule();

	public static ArrayParseRule getInstance() {
		return instance;
	}

	private ArrayParseRule() {
	}

	/**
	 * Patterns: 1. {a1, ..., an} 2. <type>[size]...[]
	 */
	public ArrayNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		int type = visitType(tokenizer);
		if (type != -1) {
			expectSymbol(SymbolToken.SQUARE_BRACES_LEFT, tokenizer);
			ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
			if (index == null) {
				throw new ParseException("array dimension missing", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.SQUARE_BRACES_RIGHT, tokenizer);

			ArrayNode node = new ArrayNode(type);
			node.addIndex(index);

			while (visitSymbol(tokenizer, SymbolToken.SQUARE_BRACES_LEFT) != -1) {
				index = ExpressionParseRule.getInstance().visit(tokenizer);
				expectSymbol(SymbolToken.SQUARE_BRACES_RIGHT, tokenizer);

				node.addIndex(index);
				if (index == null) {
					break;
				}
			}

			while (visitSymbol(tokenizer, SymbolToken.MASSIVE) != -1) {
				node.addIndex(null);
			}

			return node;
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int type = visitType(tokenizer, handler);
		if (type != -1) {
			expectSymbol(SymbolToken.SQUARE_BRACES_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "array dimension missing");
			}
			expectSymbol(SymbolToken.SQUARE_BRACES_RIGHT, tokenizer, handler);

			while (visitSymbol(tokenizer, handler, SymbolToken.SQUARE_BRACES_LEFT) != -1) {
				boolean hasExpression = ExpressionParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(SymbolToken.SQUARE_BRACES_RIGHT, tokenizer, handler);

				if (!hasExpression) {
					break;
				}
			}

			while (visitSymbol(tokenizer, handler, SymbolToken.MASSIVE) != -1) {
			}

			return true;
		}

		return false;
	}
}
