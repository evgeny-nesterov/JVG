package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.ArgumentsNode;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.MethodNode;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class MethodParseRule extends ParseRule<MethodNode> {
	private final static MethodParseRule instance = new MethodParseRule();

	public static MethodParseRule getInstance() {
		return instance;
	}

	private MethodParseRule() {
	}

	@Override
	public MethodNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		WordType type = visitType(tokenizer);
		if (type != null) {
			int dimension = visitDimension(tokenizer);
			String name = visitWord(WordType.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
					tokenizer.commit();
					ArgumentsNode arguments = ArgumentsParseRule.getInstance().visit(tokenizer);
					expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);

					expectSymbol(SymbolType.BRACES_LEFT, tokenizer);
					BlockNode body = BlockParseRule.getInstance().visit(tokenizer);
					expectSymbol(SymbolType.BRACES_RIGHT, tokenizer);

					return new MethodNode(name, type, dimension, arguments, body);
				}
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		WordType type = visitType(tokenizer, handler);
		if (type != null) {
			visitDimension(tokenizer, handler);
			String name = visitWord(WordType.NOT_SERVICE, tokenizer, handler);
			if (name != null) {
				if (visitSymbol(tokenizer, handler, SymbolType.PARENTHESES_LEFT) != null) {
					tokenizer.commit();
					ArgumentsParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);

					expectSymbol(SymbolType.BRACES_LEFT, tokenizer, handler);
					BlockParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(SymbolType.BRACES_RIGHT, tokenizer, handler);

					return true;
				}
			}
		}
		tokenizer.rollback();

		return false;
	}
}
