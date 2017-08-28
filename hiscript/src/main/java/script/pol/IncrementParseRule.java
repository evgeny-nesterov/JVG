package script.pol;

import script.ParseException;
import script.pol.model.IncrementNode;
import script.pol.model.VariableNode;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class IncrementParseRule extends ParseRule<IncrementNode> {
	private final static IncrementParseRule instance = new IncrementParseRule();

	public static IncrementParseRule getInstance() {
		return instance;
	}

	private IncrementParseRule() {
	}

	public IncrementNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		int operation = visitSymbol(tokenizer, SymbolToken.PLUS_PLUS, SymbolToken.MINUS_MINUS);

		tokenizer.start();
		if (operation != -1) {
			VariableNode variable = visitVariable(tokenizer);
			if (variable == null) {
				throw new ParseException("variable is expected", tokenizer.currentToken());
			}
			tokenizer.commit();
			return new IncrementNode(variable, IncrementNode.INCREMENT_PREFIX, operation);
		}
		tokenizer.rollback();

		tokenizer.start();
		VariableNode variable = visitVariable(tokenizer);
		if (variable != null) {
			operation = visitSymbol(tokenizer, SymbolToken.PLUS_PLUS, SymbolToken.MINUS_MINUS);
			if (operation != -1) {
				tokenizer.commit();
				return new IncrementNode(variable, IncrementNode.INCREMENT_POSTFIX, operation);
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int operation = visitSymbol(tokenizer, handler, SymbolToken.PLUS_PLUS, SymbolToken.MINUS_MINUS);
		tokenizer.start();

		if (operation != -1) {
			VariableNode variable = visitVariable(tokenizer, handler);
			if (variable == null) {
				errorOccured(tokenizer, handler, "variable is expected");
			}
			tokenizer.commit();
			return true;
		}

		VariableNode variable = visitVariable(tokenizer, handler);
		if (variable != null) {
			operation = visitSymbol(tokenizer, handler, SymbolToken.PLUS_PLUS, SymbolToken.MINUS_MINUS);
			if (operation != -1) {
				tokenizer.commit();
				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}
}
