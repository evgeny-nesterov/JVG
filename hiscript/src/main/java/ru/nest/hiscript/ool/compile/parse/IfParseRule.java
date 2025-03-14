package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeIf;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class IfParseRule extends ParseRule<NodeIf> {
	private final static IfParseRule instance = new IfParseRule();

	public static IfParseRule getInstance() {
		return instance;
	}

	private IfParseRule() {
	}

	@Override
	public NodeIf visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.IF, tokenizer) != null) {
			NodeExpression condition = expectCondition(tokenizer, ctx);
			HiNode body = expectBody(tokenizer, ctx);
			NodeIf elseIfNode = visitNext(tokenizer, ctx);
			return new NodeIf(condition, body, elseIfNode);
		}
		return null;
	}

	public NodeIf visitNext(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		Token startToken = startToken(tokenizer);
		if (visitWord(WordType.ELSE, tokenizer) != null) {
			if (visitWord(WordType.IF, tokenizer) != null) {
				NodeExpression condition = expectCondition(tokenizer, ctx);
				HiNode body = expectBody(tokenizer, ctx);
				NodeIf elseIfNode = visitNext(tokenizer, ctx);
				NodeIf ifNode = new NodeIf(condition, body, elseIfNode);
				ifNode.setToken(tokenizer.getBlockToken(startToken));
				return ifNode;
			} else {
				HiNode body = expectBody(tokenizer, ctx);
				NodeIf ifNode = new NodeIf(null, body, null);
				ifNode.setToken(tokenizer.getBlockToken(startToken));
				return ifNode;
			}
		}
		return null;
	}
}
