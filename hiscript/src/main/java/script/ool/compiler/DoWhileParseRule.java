package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeBlock;
import script.ool.model.nodes.NodeDoWhile;
import script.ool.model.nodes.NodeExpression;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class DoWhileParseRule extends ParseRule<NodeDoWhile> {
	private final static DoWhileParseRule instance = new DoWhileParseRule();

	public static DoWhileParseRule getInstance() {
		return instance;
	}

	private DoWhileParseRule() {
	}

	public NodeDoWhile visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.DO, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			NodeBlock body = BlockParseRule.getInstance().visit(tokenizer, properties);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			expectWord(WordToken.DO, tokenizer);
			NodeExpression condition = expectCondition(tokenizer, properties);

			NodeDoWhile node = new NodeDoWhile(body, condition);
			return node;
		}
		return null;
	}
}
