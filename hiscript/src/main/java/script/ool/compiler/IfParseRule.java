package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeIf;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class IfParseRule extends ParseRule<NodeIf> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	public NodeIf visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.IF, tokenizer) != null) {
			NodeExpression condition = expectCondition(tokenizer, properties);
			Node body = expectBody(tokenizer, properties);
			NodeIf elseIfNode = visitNext(tokenizer, properties);

			NodeIf node = new NodeIf(condition, body, elseIfNode);
			return node;
		}
		return null;
	}

	public NodeIf visitNext(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.ELSE, tokenizer) != null) {
			if (visitWord(WordToken.IF, tokenizer) != null) {
				NodeExpression condition = expectCondition(tokenizer, properties);
				Node body = expectBody(tokenizer, properties);
				NodeIf elseIfNode = visitNext(tokenizer, properties);

				NodeIf node = new NodeIf(condition, body, elseIfNode);
				return node;
			} else {
				Node body = expectBody(tokenizer, properties);

				NodeIf node = new NodeIf(null, body, null);
				return node;
			}
		}
		return null;
	}
}
