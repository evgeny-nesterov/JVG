package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.CaseNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.SwitchNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class SwitchParseRule extends ParseRule<SwitchNode> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	@Override
	public SwitchNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(Words.SWITCH, tokenizer) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer);

			Node value = ExpressionParseRule.getInstance().visit(tokenizer);
			if (value == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);

			SwitchNode node = new SwitchNode(value);

			boolean defaultFound = false;
			expectSymbol(Symbols.BRACES_LEFT, tokenizer);
			while (true) {
				CaseNode caseNode = CaseParseRule.getInstance().visit(tokenizer);
				if (caseNode != null) {
					node.addCase(caseNode);
					continue;
				}

				if (visitWord(Words.DEFAULT, tokenizer) != null) {
					expectSymbol(Symbols.COLON, tokenizer);
					if (defaultFound) {
						throw new ParseException("dublicate default label", tokenizer.currentToken());
					}

					BlockNode defaultBody = BlockParseRule.getInstance().visit(tokenizer);
					node.setDefault(defaultBody);

					defaultFound = true;
					continue;
				}

				break;
			}
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer);

			return node;
		}

		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(Words.SWITCH, tokenizer, handler) != null) {
			expectSymbol(Symbols.PARANTHESIS_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression is expected");
			}
			expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);

			boolean defaultFound = false;
			expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
			while (true) {
				if (CaseParseRule.getInstance().visit(tokenizer, handler)) {
					continue;
				}

				Token token = tokenizer.currentToken();
				if (visitWord(Words.DEFAULT, tokenizer, handler) != null) {
					if (defaultFound) {
						errorOccurred(handler, "dublicate default label", token);
					}
					expectSymbol(Symbols.COLON, tokenizer, handler);

					BlockParseRule.getInstance().visit(tokenizer, handler);
					defaultFound = true;
					continue;
				}

				break;
			}
			expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);

			return true;
		}

		return false;
	}
}
