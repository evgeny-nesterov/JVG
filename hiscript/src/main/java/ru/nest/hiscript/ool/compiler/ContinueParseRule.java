package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeContinue;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ContinueParseRule extends ParseRule<NodeContinue> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	@Override
	public NodeContinue visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		if (visitWord(Words.CONTINUE, tokenizer) != null) {
			String label = visitWord(Words.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeContinue node = new NodeContinue(label);
			return node;
		}
		return null;
	}
}
