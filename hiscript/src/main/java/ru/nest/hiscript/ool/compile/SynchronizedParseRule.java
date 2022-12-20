package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeSynchronized;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class SynchronizedParseRule extends ParseRule<NodeSynchronized> {
	private final static SynchronizedParseRule instance = new SynchronizedParseRule();

	public static SynchronizedParseRule getInstance() {
		return instance;
	}

	private SynchronizedParseRule() {
	}

	@Override
	public NodeSynchronized visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.SYNCHRONIZED, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);
			HiNode lock = expectExpression(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			HiNode body = BlockParseRule.getInstance().visit(tokenizer, ctx);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			return new NodeSynchronized(lock, body);
		}
		return null;
	}
}
