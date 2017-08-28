package script.pol;

import script.ParseException;
import script.pol.model.ArgumentNode;
import script.pol.model.ArgumentsNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class ArgumentsParseRule extends ParseRule<ArgumentsNode> {
	private final static ArgumentsParseRule instance = new ArgumentsParseRule();

	public static ArgumentsParseRule getInstance() {
		return instance;
	}

	private ArgumentsParseRule() {
	}

	public ArgumentsNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		ArgumentNode argument = ArgumentParseRule.getInstance().visit(tokenizer);
		if (argument != null) {
			ArgumentsNode node = new ArgumentsNode();
			node.addArgument(argument);

			while (visitSymbol(tokenizer, SymbolToken.COMMA) != -1) {
				argument = ArgumentParseRule.getInstance().visit(tokenizer);
				if (argument == null) {
					throw new ParseException("argument expected", tokenizer.currentToken());
				}
				node.addArgument(argument);
			}

			return node;
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (ArgumentParseRule.getInstance().visit(tokenizer, handler)) {
			while (visitSymbol(tokenizer, handler, SymbolToken.COMMA) != -1) {
				if (!ArgumentParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "argument expected");
				}
			}

			return true;
		}

		return false;
	}
}
