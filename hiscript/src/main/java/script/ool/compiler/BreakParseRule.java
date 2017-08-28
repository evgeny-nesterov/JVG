package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeBreak;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class BreakParseRule extends ParseRule<NodeBreak> {
	private final static BreakParseRule instance = new BreakParseRule();

	public static BreakParseRule getInstance() {
		return instance;
	}

	private BreakParseRule() {
	}

	public NodeBreak visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.BREAK, tokenizer) != null) {
			String label = visitWord(WordToken.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeBreak node = new NodeBreak(label);
			return node;
		}
		return null;
	}
}
