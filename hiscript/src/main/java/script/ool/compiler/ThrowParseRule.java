package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeThrow;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class ThrowParseRule extends ParseRule<NodeThrow> {
	private final static ThrowParseRule instance = new ThrowParseRule();

	public static ThrowParseRule getInstance() {
		return instance;
	}

	private ThrowParseRule() {
	}

	@Override
	public NodeThrow visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.THROW, tokenizer) != null) {
			NodeThrow node = new NodeThrow(expectExpression(tokenizer, properties));
			return node;
		}
		return null;
	}
}
