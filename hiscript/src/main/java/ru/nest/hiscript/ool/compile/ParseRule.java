package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public abstract class ParseRule<N extends HiNode> extends ParserUtil {
	public N visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		Token startToken = startToken(tokenizer);
		N node = visit(tokenizer, ctx, startToken);
		if (node != null) {
			node.setToken(tokenizer.getBlockToken(startToken));
		}
		return node;
	}

	public N visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		return null;
	}
}
