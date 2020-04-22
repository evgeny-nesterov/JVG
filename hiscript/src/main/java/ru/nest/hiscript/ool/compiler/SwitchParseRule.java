package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.nodes.NodeBlock;
import ru.nest.hiscript.ool.model.nodes.NodeExpression;
import ru.nest.hiscript.ool.model.nodes.NodeSwitch;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class SwitchParseRule extends ParseRule<NodeSwitch> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	@Override
	public NodeSwitch visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.SWITCH, tokenizer) != null) {
			NodeExpression value = expectCondition(tokenizer, properties);
			NodeSwitch node = new NodeSwitch(value);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			while (true) {
				if (visitWord(Words.CASE, tokenizer) != null) {
					NodeExpression caseValue = expectExpression(tokenizer, properties);
					expectSymbol(tokenizer, Symbols.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, properties);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, properties);

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
