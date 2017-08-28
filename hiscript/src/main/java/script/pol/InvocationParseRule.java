package script.pol;

import script.ParseException;
import script.pol.model.InvocationNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class InvocationParseRule extends ParseRule<InvocationNode> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	public InvocationNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		String namespace = null;
		String methodName = visitWord(WordToken.NOT_SERVICE, tokenizer);
		if (visitSymbol(tokenizer, SymbolToken.POINT) != -1) {
			namespace = methodName;
			methodName = visitWord(WordToken.NOT_SERVICE, tokenizer);
		}

		if (methodName != null) {
			if (visitSymbol(tokenizer, SymbolToken.PARANTHESIS_LEFT) != -1) {
				tokenizer.commit();
				InvocationNode node = new InvocationNode(namespace, methodName);

				Node argument = ExpressionParseRule.getInstance().visit(tokenizer);
				if (argument != null) {
					node.addArgument(argument);

					while (visitSymbol(tokenizer, SymbolToken.COMMA) != -1) {
						argument = ExpressionParseRule.getInstance().visit(tokenizer);
						if (argument == null) {
							throw new ParseException("Argument is expected", tokenizer.currentToken());
						}
						node.addArgument(argument);
					}
				}

				expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer);
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		String namespace = null;
		String methodName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
		if (visitSymbol(tokenizer, handler, SymbolToken.POINT) != -1) {
			namespace = methodName;
			methodName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
		}

		// String methodName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
		if (methodName != null) {
			if (visitSymbol(tokenizer, handler, SymbolToken.PARANTHESIS_LEFT) != -1) {
				tokenizer.commit();

				if (ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
					while (visitSymbol(tokenizer, handler, SymbolToken.COMMA) != -1) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccured(tokenizer, handler, "Argument is expected");
						}
					}
				}

				expectSymbol(SymbolToken.PARANTHESIS_RIGHT, tokenizer, handler);
				return true;
			}
		}
		tokenizer.rollback();

		return false;
	}
}
