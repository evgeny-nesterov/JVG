package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeContinue;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ContinueParseRule extends ParseRule<NodeContinue> {
	private final static ContinueParseRule instance = new ContinueParseRule();

	public static ContinueParseRule getInstance() {
		return instance;
	}

	private ContinueParseRule() {
	}

	public NodeContinue visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.CONTINUE, tokenizer) != null) {
			String label = visitWord(WordToken.NOT_SERVICE, tokenizer);
			expectSymbol(tokenizer, Symbols.SEMICOLON);

			NodeContinue node = new NodeContinue(label);
			return node;
		}
		return null;
	}
}
