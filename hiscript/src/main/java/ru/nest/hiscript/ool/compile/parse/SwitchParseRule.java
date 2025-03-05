package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeCastedIdentifier;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeSwitch;
import ru.nest.hiscript.tokenizer.SymbolType;
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

			expectSymbol(tokenizer, SymbolType.BRACES_LEFT);

			while (true) {
				if (visitWord(Words.CASE, tokenizer) != null) {
					HiNode[] caseValue = visitCaseValue(tokenizer, ctx);
					expectSymbol(tokenizer, SymbolType.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, ctx);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(tokenizer, SymbolType.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, ctx);

					node.add(null, caseBody);
					continue;
				}
				break;
			}

			expectSymbol(tokenizer, SymbolType.BRACES_RIGHT);
			return node;
		}
		return null;
	}

	protected static HiNode[] visitCaseValue(Tokenizer tokenizer, CompileClassContext ctx) throws TokenizerException, HiScriptParseException {
		List<HiNode> args = new ArrayList<>(3);
		NodeExpression arg = ExpressionParseRule.castedIdentifierPriority.visit(tokenizer, ctx);
		if (arg != null) {
			NodeCastedIdentifier identifier = arg.checkCastedIdentifier();
			if (identifier != null && visitWord(Words.WHEN, tokenizer) != null) {
				identifier.castedCondition = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
			}
			args.add(arg);
			while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
				arg = ExpressionParseRule.castedIdentifierPriority.visit(tokenizer, ctx);
				if (arg != null) {
					identifier = arg.checkCastedIdentifier();
					if (identifier != null && visitWord(Words.WHEN, tokenizer) != null) {
						identifier.castedCondition = ExpressionParseRule.methodPriority.visit(tokenizer, ctx);
					}
					args.add(arg);
				} else {
					tokenizer.error("expression expected");
				}
			}
		}

		HiNode[] argsArray = new HiNode[args.size()];
		args.toArray(argsArray);
		return argsArray;
	}
}
