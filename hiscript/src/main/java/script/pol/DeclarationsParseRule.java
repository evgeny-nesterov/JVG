package script.pol;

import script.ParseException;
import script.pol.model.DeclarationsNode;
import script.pol.model.Node;
import script.tokenizer.SymbolToken;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;

public class DeclarationsParseRule extends ParseRule<DeclarationsNode> {
	private final static DeclarationsParseRule instance = new DeclarationsParseRule();

	public static DeclarationsParseRule getInstance() {
		return instance;
	}

	private DeclarationsParseRule() {
	}

	public DeclarationsNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		int type = visitType(tokenizer);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer);

			String namespace = null;
			String variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
			if (visitSymbol(tokenizer, SymbolToken.POINT) != -1) {
				namespace = variableName;
				variableName = visitWord(WordToken.NOT_SERVICE, tokenizer);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer);
				DeclarationsNode node = new DeclarationsNode(type);

				Node value = null;
				if (visitSymbol(tokenizer, SymbolToken.EQUATE) != -1) {
					value = ExpressionParseRule.getInstance().visit(tokenizer);
					if (value == null) {
						throw new ParseException("expression is expected", tokenizer.currentToken());
					}
				}
				node.addVariable(namespace, variableName, dimension, value);

				while (visitSymbol(tokenizer, SymbolToken.COMMA) != -1) {
					if ((variableName = visitWord(WordToken.NOT_SERVICE, tokenizer)) == null) {
						throw new ParseException("<identifier> is expected", tokenizer.currentToken());
					}
					dimension = commonDimension + visitDimension(tokenizer);

					value = null;
					if (visitSymbol(tokenizer, SymbolToken.EQUATE) != -1) {
						value = ExpressionParseRule.getInstance().visit(tokenizer);
						if (value == null) {
							throw new ParseException("expression is expected", tokenizer.currentToken());
						}
					}
					node.addVariable(namespace, variableName, dimension, value);
				}
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		int type = visitType(tokenizer, handler);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer, handler);

			String namespace = null;
			String variableName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
			if (visitSymbol(tokenizer, handler, SymbolToken.POINT) != -1) {
				namespace = variableName;
				variableName = visitWord(WordToken.NOT_SERVICE, tokenizer, handler);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer, handler);
				if (visitSymbol(tokenizer, handler, SymbolToken.EQUATE) != -1) {
					if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
						errorOccured(tokenizer, handler, "expression is expected");
					}
				}

				while (visitSymbol(tokenizer, handler, SymbolToken.COMMA) != -1) {
					if (visitWord(WordToken.NOT_SERVICE, tokenizer, handler) == null) {
						errorOccured(tokenizer, handler, "<identifier> is expected");
					}
					dimension = commonDimension + visitDimension(tokenizer, handler);

					if (visitSymbol(tokenizer, handler, SymbolToken.EQUATE) != -1) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccured(tokenizer, handler, "expression is expected");
						}
					}
				}

				return true;
			}
		}

		tokenizer.rollback();
		return false;
	}
}
