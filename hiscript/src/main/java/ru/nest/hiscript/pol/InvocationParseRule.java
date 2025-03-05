package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.InvocationNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class InvocationParseRule extends ParseRule<InvocationNode> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	@Override
	public InvocationNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		String namespace = null;
		String methodName = visitWord(Words.NOT_SERVICE, tokenizer);
		if (visitSymbol(tokenizer, SymbolType.POINT) != null) {
			namespace = methodName;
			methodName = visitWord(Words.NOT_SERVICE, tokenizer);
		}

		if (methodName != null) {
			if (visitSymbol(tokenizer, SymbolType.PARENTHESES_LEFT) != null) {
				tokenizer.commit();
				InvocationNode node = new InvocationNode(namespace, methodName);

				Node argument = ExpressionParseRule.getInstance().visit(tokenizer);
				if (argument != null) {
					node.addArgument(argument);

					while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
						argument = ExpressionParseRule.getInstance().visit(tokenizer);
						if (argument == null) {
							throw new HiScriptParseException("Argument is expected", tokenizer.currentToken());
						}
						node.addArgument(argument);
					}
				}

				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer);
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		// TODO String namespace = null;
		String methodName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
		if (visitSymbol(tokenizer, handler, SymbolType.POINT) != null) {
			// TODO namespace = methodName;
			methodName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
		}

		// String methodName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
		if (methodName != null) {
			if (visitSymbol(tokenizer, handler, SymbolType.PARENTHESES_LEFT) != null) {
				tokenizer.commit();

				if (ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					while (visitSymbol(tokenizer, handler, SymbolType.COMMA) != null) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccurred(tokenizer, handler, "Argument is expected");
						}
					}
				}

				expectSymbol(SymbolType.PARENTHESES_RIGHT, tokenizer, handler);
				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
