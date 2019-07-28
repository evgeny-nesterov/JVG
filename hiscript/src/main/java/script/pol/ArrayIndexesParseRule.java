package script.pol;

import script.ParseException;
import script.pol.model.ArrayIndexesNode;
import script.pol.model.ExpressionNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class ArrayIndexesParseRule extends ParseRule<ArrayIndexesNode> {
	private final static ArrayIndexesParseRule instance = new ArrayIndexesParseRule();

	public static ArrayIndexesParseRule getInstance() {
		return instance;
	}

	private ArrayIndexesParseRule() {
	}

	@Override
	public ArrayIndexesNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		ArrayIndexesNode indexes = null;

		while (visitSymbol(tokenizer, Symbols.SQUARE_BRACES_LEFT) != -1) {
			ExpressionNode index = ExpressionParseRule.getInstance().visit(tokenizer);
			if (index == null) {
				throw new ParseException("expression expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer);

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
		while (visitSymbol(tokenizer, handler, Symbols.SQUARE_BRACES_LEFT) != -1) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression expected");
			}
			expectSymbol(Symbols.SQUARE_BRACES_RIGHT, tokenizer, handler);
			found = true;
		}

		return found;
	}
}
