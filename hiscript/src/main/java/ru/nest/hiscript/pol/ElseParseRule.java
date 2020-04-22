package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.IfNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class ElseParseRule extends ParseRule<IfNode> {
	private final static ElseParseRule instance = new ElseParseRule();

	public static ElseParseRule getInstance() {
		return instance;
	}

	private ElseParseRule() {
	}

	@Override
	public IfNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.ELSE, tokenizer) != null) {
			Node body = StatementParseRule.getInstance().visit(tokenizer);
			if (body == null) {
				throw new ParseException("Statement is expected", tokenizer.currentToken());
			}

			return new IfNode(null, body);
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.ELSE, tokenizer, handler) != null) {
			if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "Statement is expected");
			}

			return true;
		}

		return false;
	}
}
