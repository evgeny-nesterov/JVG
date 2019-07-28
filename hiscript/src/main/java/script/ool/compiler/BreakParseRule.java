package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeBreak;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class BreakParseRule extends ParseRule<NodeBreak> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	@Override
	public NodeBreak visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.BREAK, tokenizer) != null) {
			String label = visitWord(Words.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeBreak node = new NodeBreak(label);
			return node;
		}
		return null;
	}
}
