package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.ArgumentsNode;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.MethodNode;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class MethodParseRule extends ParseRule<MethodNode> {
	private final static MethodParseRule instance = new MethodParseRule();

	public static MethodParseRule getInstance() {
		return instance;
	}

	private MethodParseRule() {
	}

	@Override
	public MethodNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		int type = visitType(tokenizer);
		if (type != -1) {
			int dimension = visitDimension(tokenizer);
			String name = visitWord(Words.NOT_SERVICE, tokenizer);
			if (name != null) {
				if (visitSymbol(tokenizer, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					ArgumentsNode arguments = ArgumentsParseRule.getInstance().visit(tokenizer);
					expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer);

					expectSymbol(Symbols.BRACES_LEFT, tokenizer);
					BlockNode body = BlockParseRule.getInstance().visit(tokenizer);
					expectSymbol(Symbols.BRACES_RIGHT, tokenizer);

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

		int type = visitType(tokenizer, handler);
		if (type != -1) {
			visitDimension(tokenizer, handler);
			String name = visitWord(Words.NOT_SERVICE, tokenizer, handler);
			if (name != null) {
				if (visitSymbol(tokenizer, handler, Symbols.PARENTHESES_LEFT) != -1) {
					tokenizer.commit();
					ArgumentsParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(Symbols.PARENTHESES_RIGHT, tokenizer, handler);

					expectSymbol(Symbols.BRACES_LEFT, tokenizer, handler);
					BlockParseRule.getInstance().visit(tokenizer, handler);
					expectSymbol(Symbols.BRACES_RIGHT, tokenizer, handler);

					return true;
				}
			}
		}
		tokenizer.rollback();

		return false;
	}
}
