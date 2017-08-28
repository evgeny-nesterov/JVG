package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeReturn;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class ReturnParseRule extends ParseRule<NodeReturn> {
	private final static ReturnParseRule instance = new ReturnParseRule();

	public static ReturnParseRule getInstance() {
		return instance;
	}

	private ReturnParseRule() {
	}

	public NodeReturn visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.RETURN, tokenizer) != null) {
			NodeExpression value = ExpressionParseRule.getInstance().visit(tokenizer, properties);
			NodeReturn node = new NodeReturn(value);
			return node;
		}
		return null;
	}
}
