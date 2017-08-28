package script.pol;

import script.ParseException;
import script.pol.model.ArgumentsNode;
import script.pol.model.BlockNode;
import script.pol.model.MethodNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class MethodParseRule extends ParseRule<MethodNode> {
	private final static MethodParseRule instance = new MethodParseRule();

	public static MethodParseRule getInstance() {
		return instance;
	}

	private MethodParseRule() {
	}

	public MethodNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		int type = visitType(tokenizer);
		if (type != -1) {
			int dimension = visitDimension(tokenizer);
			String name = visitWord(WordToken.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT) != -1) {
					tokenizer.commit();
					ArgumentsNode arguments = ArgumentsParseRule.getInstance().visit(tokenizer);
					expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);

					expectSymbol(SymbolToken.BRACES_LEFT, tokenizer);
					BlockNode body = BlockParseRule.getInstance().visit(tokenizer);
					expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer);

					return new MethodNode(name, type, dimension, arguments, body);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		int type = visitType(tokenizer, handler);
		if (type != -1) {
			visitDimension(tokenizer, handler);
			String name = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
			if (name != null) {
				if (visitSymbol(tokenizer, handler, SymbolToken.PARANTHESIS_LEFT) != -1) {
					tokenizer.commit();
					ArgumentsParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);

					expectSymbol(SymbolToken.BRACES_LEFT, tokenizer, handler);
					BlockParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer, handler);

					return true;
				}
			}
		}
		tokenizer.rollback();

		return false;
	}
}
