package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.RuntimeContext;
import ru.nest.hiscript.ool.model.nodes.EmptyNode;
import ru.nest.hiscript.ool.model.nodes.NodeAssert;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
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
	public NodeBlock visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		ctx.enter(RuntimeContext.BLOCK, startToken);

		NodeBlock block = null;
		HiNode statement;
		while ((statement = StatementParseRule.getInstance().visit(tokenizer, ctx)) != null) {
			if (statement == EmptyNode.getInstance()) {
				continue;
			}

			if (!ctx.getCompiler().isAssertsActive() && statement instanceof NodeAssert) {
				continue;
			}

			// add to block
			if (block == null) {
				block = new NodeBlock();
			}
			block.addStatement(statement);
		}

		ctx.exit();
		return block;
	}
}
