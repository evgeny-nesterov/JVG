package script.pol;

import script.ParseException;
import script.pol.model.BlockNode;
import script.pol.model.CaseNode;
import script.pol.model.Node;
import script.pol.model.SwitchNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Token;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class SwitchParseRule extends ParseRule<SwitchNode> {
	private final static SwitchParseRule instance = new SwitchParseRule();

	public static SwitchParseRule getInstance() {
		return instance;
	}

	private SwitchParseRule() {
	}

	public SwitchNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitWord(WordToken.SWITCH, tokenizer) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer);

			Node value = ExpressionParseRule.getInstance().visit(tokenizer);
			if (value == null) {
				throw new ParseException("expression is expected", tokenizer.currentToken());
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);

			SwitchNode node = new SwitchNode(value);

			boolean defaultFound = false;
			expectSymbol(SymbolToken.BRACES_LEFT, tokenizer);
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
			expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer);

			return node;
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		if (visitWord(WordToken.SWITCH, tokenizer, handler) != null) {
			expectSymbol(SymbolToken.PARANTHESIS_LEFT, tokenizer, handler);

			if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
				errorOccured(tokenizer, handler, "expression is expected");
			}
			expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);

			boolean defaultFound = false;
			expectSymbol(SymbolToken.BRACES_LEFT, tokenizer, handler);
			while (true) {
				if (CaseParseRule.getInstance().visit(tokenizer, handler)) {
					continue;
				}

				Token token = tokenizer.currentToken();
				if (visitWord(Words.DEFAULT, tokenizer, handler) != null) {
					if (defaultFound) {
						errorOccured(handler, "dublicate default label", token);
					}
					expectSymbol(Symbols.COLON, tokenizer, handler);

					BlockParseRule.getInstance().visit(tokenizer, handler);
					defaultFound = true;
					continue;
				}

				break;
			}
			expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer, handler);

			return true;
		}

		return false;
	}
}
