package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiNodeIF;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public abstract class ParseRule<N extends HiNodeIF> extends ParserUtil {
	public N visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		N node = visit(tokenizer, ctx, startToken);
		if (node != null) {
			node.setToken(tokenizer.getBlockToken(startToken));
		}
		return node;
	}

	public N visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		return null;
	}
}
