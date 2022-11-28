package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeExpressionSwitch;
import ru.nest.hiscript.tokenizer.Symbols;
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
	public NodeExpressionSwitch visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.SWITCH, tokenizer) != null) {
			NodeExpression value = expectCondition(tokenizer, properties);
			NodeExpressionSwitch node = new NodeExpressionSwitch(value);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			while (true) {
				if (visitWord(Words.CASE, tokenizer) != null) {
					Node[] caseValue = visitArgumentsValues(tokenizer, properties);
					expectSymbol(tokenizer, Symbols.REFERENCE);
					NodeExpression caseBody = expectExpression(tokenizer, properties);
					expectSymbol(tokenizer, Symbols.SEMICOLON);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.REFERENCE);
					NodeExpression caseBody = expectExpression(tokenizer, properties);
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
