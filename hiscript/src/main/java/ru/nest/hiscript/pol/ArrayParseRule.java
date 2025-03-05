package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArrayNode;
import ru.nest.hiscript.pol.model.ExpressionNode;
import ru.nest.hiscript.tokenizer.SymbolType;
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
	public ArrayNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		int type = visitType(tokenizer);
		if (type != -1) {
			expectSymbol(SymbolType.SQUARE_BRACES_LEFT, tokenizer);
			ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
			if (index == null) {
				throw new HiScriptParseException("array dimension missing", tokenizer.currentToken());
			}
			expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer);

			ArrayNode node = new ArrayNode(type);
			node.addIndex(index);

			while (visitSymbol(tokenizer, SymbolType.SQUARE_BRACES_LEFT) != null) {
				index = ExpressionParseRule.getInstance().visit(tokenizer);
				expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer);

				node.addIndex(index);
				if (index == null) {
					break;
				}
			}

			while (visitSymbol(tokenizer, SymbolType.MASSIVE) != null) {
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
			expectSymbol(SymbolType.SQUARE_BRACES_LEFT, tokenizer, handler);
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "array dimension missing");
			}
			expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer, handler);

			while (visitSymbol(tokenizer, handler, SymbolType.SQUARE_BRACES_LEFT) != null) {
				boolean hasExpression = ExpressionParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer, handler);

				if (!hasExpression) {
					break;
				}
			}

			while (visitSymbol(tokenizer, handler, SymbolType.MASSIVE) != null) {
			}
			return true;
		}
		return false;
	}
}
