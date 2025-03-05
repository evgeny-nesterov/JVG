package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.CastNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class CastParseRule extends ParseRule<CastNode> {
	private final static CastParseRule instance = new CastParseRule();

	public static CastParseRule getInstance() {
		return instance;
	}

	private CastParseRule() {
	}

	@Override
	public CastNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.start();
		if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
			WordType type = visitType(tokenizer);
			if (type != null) {
				int dimension = visitDimension(tokenizer);

				if (dimension == 0) {
					if (visitSymbol(tokenizer, SymbolType.PARENTHESES_RIGHT) != null) {
						tokenizer.commit();
						return new CastNode(type, 0);
					}
				} else {
					tokenizer.commit();
					expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);
					return new CastNode(type, dimension);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();
		if (visitSymbol(tokenizer, handler, SymbolType.PARENTHESES_LEFT) != null) {
			WordType type = visitType(tokenizer, handler);
			if (type != null) {
				int dimension = visitDimension(tokenizer, handler);
				if (dimension == 0) {
					if (visitSymbol(tokenizer, handler, SymbolType.PARENTHESES_RIGHT) != null) {
						tokenizer.commit();
						return true;
					}
				} else {
					tokenizer.commit();
					expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}
}
