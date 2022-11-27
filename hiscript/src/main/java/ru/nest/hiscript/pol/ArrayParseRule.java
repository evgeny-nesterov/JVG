package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.ArrayNode;
import ru.nest.hiscript.pol.model.ExpressionNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

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
	@Override
	public ArrayNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		int type = visitType(tokenizer);
		if (type != -1) {
			expectSymbol(Symbols.SQUARE_BRACES_LEFT, tokenizer);
			ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
			if (index == null) {
				throw new ParseException("array dimension missing", tokenizer.currentToken());
			}
			expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer);

			ArrayNode node = new ArrayNode(type);
			node.addIndex(index);

			while (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
				index = ExpressionParseRule.getInstance().visit(tokenizer);
				expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer);

				node.addIndex(index);
				if (index == null) {
					break;
				}
			}

			while (visitSymbol(tokenizer, Symbols.MASSIVE) != -1) {
				node.addIndex(null);
			}

			return node;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int type = visitType(tokenizer, handler);
		if (type != -1) {
			expectSymbol(Symbols.SQUARE_BRACES_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "array dimension missing");
			}
			expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer, handler);

			while (visitSymbol(tokenizer, handler, Symbols.SQUARE_BRACES_LEFT) != -1) {
				boolean hasExpression = ExpressionParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer, handler);

				if (!hasExpression) {
					break;
				}
			}

			while (visitSymbol(tokenizer, handler, Symbols.MASSIVE) != -1) {
			}

			return true;
		}

		return false;
	}
}
