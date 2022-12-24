package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.EmptyNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class BlockParseRule extends ParseRule<BlockNode> {
	private final static BlockParseRule instance = new BlockParseRule();

	public static BlockParseRule getInstance() {
		return instance;
	}

	private BlockParseRule() {
	}

	@Override
	public BlockNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
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

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		boolean found = false;
		while (StatementParseRule.getInstance().visit(tokenizer, handler)) {
			found = true;
		}
		return found;
	}
}
