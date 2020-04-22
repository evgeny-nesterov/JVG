package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.CaseNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class CaseParseRule extends ParseRule<CaseNode> {
	private final static CaseParseRule instance = new CaseParseRule();

	public static CaseParseRule getInstance() {
		return instance;
	}

	private CaseParseRule() {
	}

	@Override
	public CaseNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.CASE, tokenizer) != null) {
			Node value;
			if ((value = ExpressionParseRule.getInstance().visit(tokenizer)) == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.COLON, tokenizer);
			BlockNode body = BlockParseRule.getInstance().visit(tokenizer);
			return new CaseNode(value, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.CASE, tokenizer, handler) != null) {
			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(Symbols.COLON, tokenizer, handler);
			BlockParseRule.getInstance().visit(tokenizer, handler);
			return true;
		}

		return false;
	}
}
