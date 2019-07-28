package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.NodeSynchronized;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

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
