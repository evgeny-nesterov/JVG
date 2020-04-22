package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.InvocationNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

public class InvocationParseRule extends ParseRule<InvocationNode> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	@Override
	public InvocationNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		String namespace = null;
		String methodName = visitWord(Words.NOT_SERVICE, tokenizer);
		if (visitSymbol(tokenizer, Symbols.POINT) != -1) {
			namespace = methodName;
			methodName = visitWord(Words.NOT_SERVICE, tokenizer);
		}

		if (methodName != null) {
			if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
				tokenizer.commit();
				InvocationNode node = new InvocationNode(namespace, methodName);

				Node argument = ExpressionParseRule.getInstance().visit(tokenizer);
				if (argument != null) {
					node.addArgument(argument);

					while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
						argument = ExpressionParseRule.getInstance().visit(tokenizer);
						if (argument == null) {
							throw new ParseException("Argument is expected", tokenizer.currentToken());
						}
						node.addArgument(argument);
					}
				}

				expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer);
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		String namespace = null;
		String methodName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
		if (visitSymbol(tokenizer, handler, Symbols.POINT) != -1) {
			namespace = methodName;
			methodName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
		}

		// String methodName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
		if (methodName != null) {
			if (visitSymbol(tokenizer, handler, Symbols.PARANTHESIS_LEFT) != -1) {
				tokenizer.commit();

				if (ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					while (visitSymbol(tokenizer, handler, Symbols.COMMA) != -1) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccured(tokenizer, handler, "Argument is expected");
						}
					}
				}

				expectSymbol(Symbols.PARANTHESIS_RIGHT, tokenizer, handler);
				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
