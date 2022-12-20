package ru.nest.hiscript.ool.compile;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.HiNode;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionSwitch;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class ExpressionSwitchParseRule extends ParseRule<NodeExpressionSwitch> {
	private final static ExpressionSwitchParseRule instance = new ExpressionSwitchParseRule();

	public static ExpressionSwitchParseRule getInstance() {
		return instance;
	}

	private ExpressionSwitchParseRule() {
	}

	@Override
	public NodeExpressionSwitch visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, ParseException {
		if (visitWord(Words.SWITCH, tokenizer) != null) {
			NodeExpression value = expectCondition(tokenizer, ctx);
			NodeExpressionSwitch node = new NodeExpressionSwitch(value);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			while (true) {
				if (visitWord(Words.CASE, tokenizer) != null) {
					HiNode[] caseValue = visitArgumentsValues(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.REFERENCE);
					NodeExpression caseBody = expectExpression(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.SEMICOLON);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.REFERENCE);
					NodeExpression caseBody = expectExpression(tokenizer, ctx);
					expectSymbol(tokenizer, Symbols.SEMICOLON);

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
}
