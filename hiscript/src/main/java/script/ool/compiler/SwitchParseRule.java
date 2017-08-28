package script.ool.compiler;

import script.ParseException;
import script.ool.model.nodes.NodeBlock;
import script.ool.model.nodes.NodeExpression;
import script.ool.model.nodes.NodeSwitch;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class SwitchParseRule extends ParseRule<NodeSwitch> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	public NodeSwitch visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(WordToken.SWITCH, tokenizer) != null) {
			NodeExpression value = expectCondition(tokenizer, properties);
			NodeSwitch node = new NodeSwitch(value);

			expectSymbol(tokenizer, Symbols.BRACES_LEFT);

			while (true) {
				if (visitWord(WordToken.CASE, tokenizer) != null) {
					NodeExpression caseValue = expectExpression(tokenizer, properties);
					expectSymbol(tokenizer, Symbols.COLON);
					NodeBlock caseBody = BlockParseRule.getInstance().visit(tokenizer, properties);

					node.add(caseValue, caseBody);
					continue;
				}

				if (visitWord(WordToken.DEFAULT, tokenizer) != null) {
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
