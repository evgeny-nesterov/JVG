package script.pol;

import script.ParseException;
import script.pol.model.BlockNode;
import script.pol.model.EmptyNode;
import script.pol.model.Node;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class BlockParseRule extends ParseRule<BlockNode> {
	private final static BlockParseRule instance = new BlockParseRule();

	public static BlockParseRule getInstance() {
		return instance;
	}

	private BlockParseRule() {
	}

	public BlockNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		BlockNode block = null;
		Node statement;
		while ((statement = StatementParseRule.getInstance().visit(tokenizer)) != null) {
			if (statement != EmptyNode.getInstance()) {
				if (block == null) {
					block = new BlockNode();
				}
				block.addStatement(statement);
			}
		}
		return block;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		boolean found = false;
		while (StatementParseRule.getInstance().visit(tokenizer, handler)) {
			found = true;
		}
		return found;
	}
}
