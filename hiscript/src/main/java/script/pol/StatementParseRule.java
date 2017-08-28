package script.pol;

import script.ParseException;
import script.pol.model.EmptyNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Token;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class StatementParseRule extends ParseRule<Node> {
	private final static StatementParseRule instance = new StatementParseRule();

	public static StatementParseRule getInstance() {
		return instance;
	}

	private StatementParseRule() {
	}

	/**
	 * Possible statements: semicolon if while do-while for switch break continue mark try-catch-finally return increment (pre-, post-) method
	 * declarations of variables + semicolon assignment + semicolon method invocation + semicolon block
	 */
	public Node visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		if (visitSymbol(tokenizer, SymbolToken.SEMICOLON) != -1) {
			return EmptyNode.getInstance();
		}

		Node node = null;
		if ((node = IfParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = WhileParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = DoWhileParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = ForParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = SwitchParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = MarkParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = ReturnParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = TryCatchParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = AssignmentParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if ((node = MethodParseRule.getInstance().visit(tokenizer)) != null) {
			return node;
		}

		if ((node = DeclarationsParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if ((node = IncrementParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if ((node = InvocationParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if ((node = BreakParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if ((node = ContinueParseRule.getInstance().visit(tokenizer)) != null) {
			expectSymbol(SymbolToken.SEMICOLON, tokenizer);
			return node;
		}

		if (visitSymbol(tokenizer, SymbolToken.BRACES_LEFT) != -1) {
			node = BlockParseRule.getInstance().visit(tokenizer);
			expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer);
			if (node != null) {
				return node;
			} else {
				return EmptyNode.getInstance();
			}
		}

		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int line = tokenizer.getLine();
		int offset = tokenizer.currentToken() != null ? tokenizer.currentToken().getOffset() : 0;
		Token lastToken = null;
		boolean found = true;

		while (true) {
			if (visitSymbol(tokenizer, handler, SymbolToken.SEMICOLON) != -1) {
				break;
			}

			if (IfParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (WhileParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (DoWhileParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (ForParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (SwitchParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (MarkParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (ReturnParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (TryCatchParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (AssignmentParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (MethodParseRule.getInstance().visit(tokenizer, handler)) {
				break;
			}

			if (DeclarationsParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (IncrementParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (InvocationParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (BreakParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (ContinueParseRule.getInstance().visit(tokenizer, handler)) {
				expectSymbol(SymbolToken.SEMICOLON, tokenizer, handler);
				break;
			}

			if (visitSymbol(tokenizer, handler, SymbolToken.BRACES_LEFT) != -1) {
				BlockParseRule.getInstance().visit(tokenizer, handler);
				expectSymbol(SymbolToken.BRACES_RIGHT, tokenizer, handler);
				break;
			}

			// check
			Token currentToken = tokenizer.currentToken();
			if (currentToken instanceof SymbolToken) {
				SymbolToken symbolToken = (SymbolToken) currentToken;
				if (symbolToken.getType() == SymbolToken.BRACES_RIGHT) {
					found = false;
					break;
				}
			}

			if (currentToken instanceof WordToken) {
				WordToken wordToken = (WordToken) currentToken;
				if (wordToken.getType() == Words.CASE || wordToken.getType() == Words.DEFAULT || wordToken.getType() == Words.CATCH || wordToken.getType() == Words.FINALLY || wordToken.getType() == Words.ELSE) {
					found = false;
					break;
				}
			}

			lastToken = tokenizer.currentToken();

			if (!tokenizer.hasNext()) {
				found = false;
				break;
			}

			try {
				tokenizer.nextToken();
			} catch (TokenizerException exc) {
				errorOccured(tokenizer, handler, exc.getMessage());
			}
		}

		if (lastToken != null) {
			handler.errorOccured(line, offset, lastToken.getOffset() + lastToken.getLength() - offset, "not a statement");
		}

		return found;
	}
}
