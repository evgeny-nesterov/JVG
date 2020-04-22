package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.ReturnNode;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class ReturnParseRule extends ParseRule<ReturnNode> {
	private final static ReturnParseRule instance = new ReturnParseRule();

	public static ReturnParseRule getInstance() {
		return instance;
	}

	private ReturnParseRule() {
	}

	@Override
	public ReturnNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.RETURN, tokenizer) != null) {
			Node returnValue = ExpressionParseRule.getInstance().visit(tokenizer);
			return new ReturnNode(returnValue);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.RETURN, tokenizer, handler) != null) {
			ExpressionParseRule.getInstance().visit(tokenizer, handler);
			return true;
		}

		return false;
	}
}
