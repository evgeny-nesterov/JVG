package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionNoLS;
import ru.nest.hiscript.ool.model.nodes.NodeSwitch;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

public class SwitchParseRule extends ParseRule<NodeSwitch> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	@Override
	public NodeSwitch visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		if (visitWord(Words.SWITCH, tokenizer) != null) {
			NodeExpression value = expectCondition(tokenizer, ctx);
			NodeSwitch node = new NodeSwitch(value);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			while (true) {
				if (visitWord(Words.CASE, tokenizer) != null) {
					HiNode[] caseValue = visitCaseValue(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, ctx);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, ctx);

					node.add(null, caseBody);
					continue;
				}
				break;
			}

			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			return node;
		}
		return null;
	}

	protected HiNode[] visitCaseValue(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		List<HiNode> args = new ArrayList<>(3);
		NodeExpression arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
		if (arg != null) {
			if (arg instanceof NodeExpressionNoLS) {
				NodeExpressionNoLS exprCaseValueNode = (NodeExpressionNoLS) arg;
				NodeCastedIdentifier identifier = exprCaseValueNode.checkCastedIdentifier();
				if (identifier != null && visitWord(Words.WHEN, tokenizer) != null) {
					identifier.castedCondition = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
				}
			}
			args.add(arg);
			while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
				arg = ExpressionParseRule.getInstance().visit(tokenizer, ctx);
				if (arg != null) {
					args.add(arg);
				} else {
					tokenizer.error("expression is expected");
				}
			}
		}

		HiNode[] argsArray = new HiNode[args.size()];
		args.toArray(argsArray);
		return argsArray;
	}
}
