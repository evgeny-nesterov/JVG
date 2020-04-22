package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeSynchronized;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class SynchronizedParseRule extends ParseRule<NodeSynchronized> {
	private final static SynchronizedParseRule instance = new SynchronizedParseRule();

	public static SynchronizedParseRule getInstance() {
		return instance;
	}

	private SynchronizedParseRule() {
	}

	@Override
	public NodeSynchronized visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.SYNCHRONIZED, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.PARANTHESIS_LEFT);
			Node lock = expectExpression(tokenizer, properties);
			expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			Node body = BlockParseRule.getInstance().visit(tokenizer, properties);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			return new NodeSynchronized(lock, body);
		}
		return null;
	}
}
