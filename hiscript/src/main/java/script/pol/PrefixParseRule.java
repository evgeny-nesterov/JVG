package script.pol;

import script.ParseException;
import script.pol.model.CastNode;
import script.pol.model.PrefixNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class PrefixParseRule extends ParseRule<PrefixNode> {
	private final static PrefixParseRule instance = new PrefixParseRule();

	public static PrefixParseRule getInstance() {
		return instance;
	}

	private PrefixParseRule() {
	}

	@Override
	public PrefixNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		PrefixNode prefix = null;
		while (true) {
			int operation = visitSymbol(tokenizer, Symbols.PLUS, Symbols.MINUS, Symbols.EXCLAMATION);
			if (operation != -1) {
				if (prefix == null) {
					prefix = new PrefixNode();
				}
				prefix.addPrefix(operation);
				continue;
			}

			CastNode node = CastParseRule.getInstance().visit(tokenizer);
			if (node != null) {
				if (prefix == null) {
					prefix = new PrefixNode();
				}
				prefix.addPrefix(node);
				continue;
			}

			break;
		}

		return prefix;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		boolean found = false;
		while (true) {
			int operation = visitSymbol(tokenizer, handler, Symbols.PLUS, Symbols.MINUS, Symbols.EXCLAMATION);
			if (operation != -1) {
				found = true;
				continue;
			}

			if (CastParseRule.getInstance().visit(tokenizer, handler)) {
				found = true;
				continue;
			}

			break;
		}

		return found;
	}
}
