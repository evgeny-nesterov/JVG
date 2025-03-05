package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.nodes.NodeBreak;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import static ru.nest.hiscript.tokenizer.Words.NOT_SERVICE;
import static ru.nest.hiscript.tokenizer.Words.UNNAMED_VARIABLE;

public class BreakParseRule extends ParseRule<NodeBreak> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	@Override
	public NodeBreak visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.BREAK, tokenizer) != null) {
			String label = visitWord(tokenizer, NOT_SERVICE, UNNAMED_VARIABLE);
			expectSymbol(tokenizer, SymbolType.SEMICOLON);
			return new NodeBreak(label);
		}
		return null;
	}
}
