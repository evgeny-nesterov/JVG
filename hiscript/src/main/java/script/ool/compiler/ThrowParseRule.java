package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeThrow;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ThrowParseRule extends ParseRule<NodeThrow> {
	private final static ThrowParseRule instance = new ThrowParseRule();

	public static ThrowParseRule getInstance() {
		return instance;
	}

	private ThrowParseRule() {
	}

	public NodeThrow visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.THROW, tokenizer) != null) {
			NodeThrow node = new NodeThrow(expectExpression(tokenizer, properties));
			return node;
		}
		return null;
	}
}
