package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.EmptyNode;
import script.ool.model.nodes.NodeBlock;
import script.ool.model.nodes.NodeReturn;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class BlockParseRule extends ParseRule<NodeBlock> {
	private final static BlockParseRule instance = new BlockParseRule();

	public static BlockParseRule getInstance() {
		return instance;
	}

	private BlockParseRule() {
	}

	public NodeBlock visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		properties.enter();

		NodeBlock block = null;
		Node statement;
		boolean isReturn = false;
		while ((statement = StatementParseRule.getInstance().visit(tokenizer, properties)) != null) {
			if (statement != EmptyNode.getInstance()) {
				// check on statement after return statement
				if (isReturn) {
					throw new ParseException("Unreachable code", tokenizer.currentToken());
				}
				isReturn = statement instanceof NodeReturn;

				// add to block
				if (block == null) {
					block = new NodeBlock();
				}
				block.addStatement(statement);
			}
		}

		properties.exit();
		return block;
	}
}
