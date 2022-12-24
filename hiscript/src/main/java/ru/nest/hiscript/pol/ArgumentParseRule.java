package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArgumentNode;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ArgumentParseRule extends ParseRule<ArgumentNode> {
	private final static ArgumentParseRule instance = new ArgumentParseRule();

	public static ArgumentParseRule getInstance() {
		return instance;
	}

	private ArgumentParseRule() {
	}

	@Override
	public ArgumentNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		int type = visitType(tokenizer);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer);

			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name == null) {
				throw new HiScriptParseException("argument expected", tokenizer.currentToken());
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
				errorOccurred(tokenizer, handler, "argument expected");
			}

			int dimension = commonDimension + visitDimension(tokenizer, handler);
			return true;
		}

		return false;
	}
}
