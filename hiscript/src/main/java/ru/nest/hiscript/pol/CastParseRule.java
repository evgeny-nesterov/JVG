package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.CastNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

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
		if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
			int type = visitType(tokenizer);
			if (type != -1) {
				int dimension = visitDimension(tokenizer);

				if (dimension == 0) {
					if (visitSymbol(tokenizer, Symbols.PARENTHESES_RIGHT) != -1) {
						tokenizer.commit();
						return new CastNode(type, 0);
					}
				} else {
					tokenizer.commit();
					expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer);
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
		if (visitSymbol(tokenizer, handler, Symbols.PARENTHESES_LEFT) != -1) {
			int type = visitType(tokenizer, handler);
			if (type != -1) {
				int dimension = visitDimension(tokenizer, handler);

				if (dimension == 0) {
					if (visitSymbol(tokenizer, handler, Symbols.PARENTHESES_RIGHT) != -1) {
						tokenizer.commit();
						return true;
					}
				} else {
					tokenizer.commit();
					expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer, handler);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}
}
