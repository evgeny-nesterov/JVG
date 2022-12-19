package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeReturn;
import ru.nest.hiscript.tokenizer.Token;
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
	public NodeBlock visit(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, ParseException {
		Token startToken = startToken(tokenizer);
		ctx.enter(RuntimeContext.BLOCK, startToken);

		NodeBlock block = null;
		HiNode statement;
		boolean isReturn = false;
		while ((statement = StatementParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			if (statement == EmptyNode.getInstance()) {
				continue;
			}

			// check on statement after return statement
			if (isReturn) {
				throw new ParseException("Unreachable statement", tokenizer.currentToken());
			}
			isReturn = statement instanceof NodeReturn;

			if (!ctx.getCompiler().isAssertsActive() && statement instanceof NodeAssert) {
				continue;
			}

			// add to block
			if (block == null) {
				block = new NodeBlock();
			}
			block.addStatement(statement);
		}
		if (block != null) {
			block.setToken(tokenizer.getBlockToken(startToken));
		}

		ctx.exit();
		return block;
	}
}
