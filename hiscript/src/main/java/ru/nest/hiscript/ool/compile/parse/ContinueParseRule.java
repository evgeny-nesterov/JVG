package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.nodes.NodeContinue;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import static ru.nest.hiscript.tokenizer.Words.NOT_SERVICE;
import static ru.nest.hiscript.tokenizer.Words.UNNAMED_VARIABLE;

public class ContinueParseRule extends ParseRule<NodeContinue> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	@Override
	public NodeContinue visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.CONTINUE, tokenizer) != null) {
			String label = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			expectSymbol(tokenizer, Symbols.SEMICOLON);
			return new NodeContinue(label);
		}
		return null;
	}
}
