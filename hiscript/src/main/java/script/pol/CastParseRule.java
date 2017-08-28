package script.pol;

import script.ParseException;
import script.pol.model.CastNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class CastParseRule extends ParseRule<CastNode> {
	private final static CastParseRule instance = new CastParseRule();

	public static CastParseRule getInstance() {
		return instance;
	}

	private CastParseRule() {
	}

	public CastNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();
		if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT) != -1) {
			int type = visitType(tokenizer);
			if (type != -1) {
				int dimension = visitDimension(tokenizer);

				if (dimension == 0) {
					if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_RIGHT) != -1) {
						tokenizer.commit();
						return new CastNode(type, 0);
					}
				} else {
					tokenizer.commit();
					expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);
					return new CastNode(type, dimension);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();
		if (visitSymbol(tokenizer, handler, SymbolToken.PARANTHESIS_LEFT) != -1) {
			int type = visitType(tokenizer, handler);
			if (type != -1) {
				int dimension = visitDimension(tokenizer, handler);

				if (dimension == 0) {
					if (visitSymbol(tokenizer, handler, SymbolToken.PARANTHESIS_RIGHT) != -1) {
						tokenizer.commit();
						return true;
					}
				} else {
					tokenizer.commit();
					expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);
					return true;
				}
			}
		}

		tokenizer.rollback();
		return false;
	}
}
