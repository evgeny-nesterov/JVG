package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.MarkNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class MarkParseRule extends ParseRule<MarkNode> {
	private final static MarkParseRule instance = new MarkParseRule();

	public static MarkParseRule getInstance() {
		return instance;
	}

	private MarkParseRule() {
	}

	@Override
	public MarkNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		String markName = visitWord(Words.NOT_SERVICE, tokenizer);
		if (markName != null) {
			if (visitSymbol(tokenizer, Symbols.COLON) != -1) {
				tokenizer.commit();

				Node body = StatementParseRule.getInstance().visit(tokenizer);
				if (body == null) {
					throw new ParseException("Statement is expected", tokenizer.currentToken());
				}

				return new MarkNode(markName, body);
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		String markName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
		if (markName != null) {
			if (visitSymbol(tokenizer, handler, Symbols.COLON) != -1) {
				tokenizer.commit();

				if (!StatementParseRule.getInstance().visit(tokenizer, handler)) {
					errorOccured(tokenizer, handler, "Statement is expected");
				}

				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
