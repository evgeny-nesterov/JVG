package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.IncrementNode;
import ru.nest.hiscript.pol.model.VariableNode;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;

public class IncrementParseRule extends ParseRule<IncrementNode> {
	private final static IncrementParseRule instance = new IncrementParseRule();

	public static IncrementParseRule getInstance() {
		return instance;
	}

	private IncrementParseRule() {
	}

	@Override
	public IncrementNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		int operation = visitSymbol(tokenizer, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);

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
			operation = visitSymbol(tokenizer, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);
			if (operation != -1) {
				tokenizer.commit();
				return new IncrementNode(variable, IncrementNode.INCREMENT_POSTFIX, operation);
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		int operation = visitSymbol(tokenizer, handler, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);
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
			operation = visitSymbol(tokenizer, handler, Symbols.PLUS_PLUS, Symbols.MINUS_MINUS);
			if (operation != -1) {
				tokenizer.commit();
				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}
}
