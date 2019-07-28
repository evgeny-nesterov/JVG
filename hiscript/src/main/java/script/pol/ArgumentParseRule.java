package script.pol;

import script.ParseException;
import script.pol.model.ArgumentNode;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class ArgumentParseRule extends ParseRule<ArgumentNode> {
	private final static ArgumentParseRule instance = new ArgumentParseRule();

	public static ArgumentParseRule getInstance() {
		return instance;
	}

	private ArgumentParseRule() {
	}

	@Override
	public ArgumentNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		int type = visitType(tokenizer);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new ParseException("argument expected", tokenizer.currentToken());
			}

			int dimension = commonDimension + visitDimension(tokenizer);
			return new ArgumentNode(type, dimension, name);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int type = visitType(tokenizer, handler);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer, handler);

			String name = visitWord(Words.NOT_SERVICE, tokenizer, handler);
			if (name == null) {
				errorOccured(tokenizer, handler, "argument expected");
			}

			int dimension = commonDimension + visitDimension(tokenizer, handler);
			return true;
		}

		return false;
	}
}
