package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.CaseNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.SwitchNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class SwitchParseRule extends ParseRule<SwitchNode> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	@Override
	public SwitchNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		if (visitWord(WordType.SWITCH, tokenizer) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer);

			Node value = ExpressionParseRule.getInstance().visit(tokenizer);
			if (value == null) {
				throw new HiScriptParseException("expression expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

			SwitchNode node = new SwitchNode(value);

			boolean defaultFound = false;
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
			while (true) {
				CaseNode caseNode = CaseParseRule.getInstance().visit(tokenizer);
				if (caseNode != null) {
					node.addCase(caseNode);
					continue;
				}

				if (visitWord(WordType.DEFAULT, tokenizer) != null) {
					expectSymbol(SymbolType.COLON, tokenizer);
					if (defaultFound) {
						throw new HiScriptParseException("duplicate default label", tokenizer.currentToken());
					}

					BlockNode defaultBody = BlockParseRule.getInstance().visit(tokenizer);
					node.setDefault(defaultBody);

					defaultFound = true;
					continue;
				}
				break;
			}
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);
			return node;
		}
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordType.SWITCH, tokenizer, handler) != null) {
			expectSymbol(SymbolType.PARENTHESES_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccurred(tokenizer, handler, "expression expected");
			}
			expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

			boolean defaultFound = false;
			expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
			while (true) {
				if (CaseParseRule.getInstance().visit(tokenizer, handler)) {
					continue;
				}

				Token token = tokenizer.currentToken();
				if (visitWord(WordType.DEFAULT, tokenizer, handler) != null) {
					if (defaultFound) {
						errorOccurred(handler, "duplicate default label", token);
					}
					expectSymbol(SymbolType.COLON, tokenizer, handler);

					BlockParseRule.getInstance().visit(tokenizer, handler);
					defaultFound = true;
					continue;
				}
				break;
			}
			expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);
			return true;
		}
		return false;
	}
}
