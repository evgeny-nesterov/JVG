package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArrayIndexesNode;
import ru.nest.hiscript.pol.model.ExpressionNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class ArrayIndexesParseRule extends ParseRule<ArrayIndexesNode> {
	private final static ArrayIndexesParseRule instance = new ArrayIndexesParseRule();

	public static ArrayIndexesParseRule getInstance() {
		return instance;
	}

	private ArrayIndexesParseRule() {
	}

	@Override
	public ArrayIndexesNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		ArrayIndexesNode indexes = null;
		while (visitSymbol(tokenizer, SymbolType.SQUARE_BRACES_LEFT) != null) {
			ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
			if (index == null) {
				throw new HiScriptParseException("expression expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer);

			if (indexes == null) {
				indexes = new ArrayIndexesNode();
			}
			indexes.addIndex(index);
		}
		return indexes;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		boolean found = false;
		while (visitSymbol(tokenizer, handler, SymbolType.SQUARE_BRACES_LEFT) != null) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}
			expectSymbol(SymbolType.SQUARE_BRACES_RIGHT, tokenizer, handler);
			found = true;
		}
		return found;
	}
}
