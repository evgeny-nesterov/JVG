package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class BlockParseRule extends ParseRule<NodeBlock> {
	private final static BlockParseRule instance = new BlockParseRule();

	public static BlockParseRule getInstance() {
		return instance;
	}

	private BlockParseRule() {
	}

	@Override
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
