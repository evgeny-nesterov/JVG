package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.IncrementNode;
import ru.nest.hiscript.pol.model.VariableNode;
import ru.nest.hiscript.tokenizer.SymbolType;
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
	public IncrementNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		SymbolType operation = visitSymbol(tokenizer, SymbolType.PLUS_PLUS, SymbolType.MINUS_MINUS);

		tokenizer.start();
		if (operation != null) {
			VariableNode variable = visitVariable(tokenizer);
			if (variable == null) {
				throw new HiScriptParseException("variable is expected", tokenizer.currentToken());
			}
			tokenizer.commit();
			return new IncrementNode(variable, IncrementNode.INCREMENT_PREFIX, operation);
		}
		tokenizer.rollback();

		tokenizer.start();
		VariableNode variable = visitVariable(tokenizer);
		if (variable != null) {
			operation = visitSymbol(tokenizer, SymbolType.PLUS_PLUS, SymbolType.MINUS_MINUS);
			if (operation != null) {
				tokenizer.commit();
				return new IncrementNode(variable, IncrementNode.INCREMENT_POSTFIX, operation);
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		SymbolType operation = visitSymbol(tokenizer, handler, SymbolType.PLUS_PLUS, SymbolType.MINUS_MINUS);
		tokenizer.start();

		if (operation != null) {
			VariableNode variable = visitVariable(tokenizer, handler);
			if (variable == null) {
				errorOccurred(tokenizer, handler, "variable is expected");
			}
			tokenizer.commit();
			return true;
		}

		VariableNode variable = visitVariable(tokenizer, handler);
		if (variable != null) {
			operation = visitSymbol(tokenizer, handler, SymbolType.PLUS_PLUS, SymbolType.MINUS_MINUS);
			if (operation != null) {
				tokenizer.commit();
				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}
}
