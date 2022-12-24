package ru.nest.hiscript.pol;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.pol.model.DeclarationsNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class DeclarationsParseRule extends ParseRule<DeclarationsNode> {
	private final static DeclarationsParseRule instance = new DeclarationsParseRule();

	public static DeclarationsParseRule getInstance() {
		return instance;
	}

	private DeclarationsParseRule() {
	}

	@Override
	public DeclarationsNode visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.start();

		int type = visitType(tokenizer);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer);

			String namespace = null;
			String variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (visitSymbol(tokenizer, Symbols.POINT) != -1) {
				namespace = variableName;
				variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer);
				DeclarationsNode node = new DeclarationsNode(type);

				Node value = null;
				if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
					value = ExpressionParseRule.getInstance().visit(tokenizer);
					if (value == null) {
						throw new ParseException("expression is expected", tokenizer.currentToken());
					}
				}
				node.addVariable(namespace, variableName, dimension, value);

				while (visitSymbol(tokenizer, Symbols.COMMA) != -1) {
					if ((variableName = visitWord(Words.NOT_SERVICE, tokenizer)) == null) {
						throw new ParseException("<identifier> is expected", tokenizer.currentToken());
					}
					dimension = commonDimension + visitDimension(tokenizer);

					value = null;
					if (visitSymbol(tokenizer, Symbols.EQUATE) != -1) {
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

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		tokenizer.start();

		int type = visitType(tokenizer, handler);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer, handler);

			// TODO String namespace = null;
			String variableName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
			if (visitSymbol(tokenizer, handler, Symbols.POINT) != -1) {
				// TODO namespace = variableName;
				variableName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer, handler);
				if (visitSymbol(tokenizer, handler, Symbols.EQUATE) != -1) {
					if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
						errorOccurred(tokenizer, handler, "expression is expected");
					}
				}

				while (visitSymbol(tokenizer, handler, Symbols.COMMA) != -1) {
					if (visitWord(Words.NOT_SERVICE, tokenizer, handler) == null) {
						errorOccurred(tokenizer, handler, "<identifier> is expected");
					}
					dimension = commonDimension + visitDimension(tokenizer, handler);

					if (visitSymbol(tokenizer, handler, Symbols.EQUATE) != -1) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccurred(tokenizer, handler, "expression is expected");
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
