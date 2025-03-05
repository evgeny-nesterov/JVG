package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.DeclarationsNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.tokenizer.SymbolType;
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
	public DeclarationsNode visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		int type = visitType(tokenizer);
		if (type != -1) {
			int commonDimension = visitDimension(tokenizer);

			String namespace = null;
			String variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			if (visitSymbol(tokenizer, SymbolType.POINT) != null) {
				namespace = variableName;
				variableName = visitWord(Words.NOT_SERVICE, tokenizer);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer);
				DeclarationsNode node = new DeclarationsNode(type);

				Node value = null;
				if (visitSymbol(tokenizer, SymbolType.EQUATE) != null) {
					value = ExpressionParseRule.getInstance().visit(tokenizer);
					if (value == null) {
						throw new HiScriptParseException("expression expected", tokenizer.currentToken());
					}
				}
				node.addVariable(namespace, variableName, dimension, value);

				while (visitSymbol(tokenizer, SymbolType.COMMA) != null) {
					if ((variableName = visitWord(Words.NOT_SERVICE, tokenizer)) == null) {
						throw new HiScriptParseException("<identifier> is expected", tokenizer.currentToken());
					}
					dimension = commonDimension + visitDimension(tokenizer);

					value = null;
					if (visitSymbol(tokenizer, SymbolType.EQUATE) != null) {
						value = ExpressionParseRule.getInstance().visit(tokenizer);
						if (value == null) {
							throw new HiScriptParseException("expression expected", tokenizer.currentToken());
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
			if (visitSymbol(tokenizer, handler, SymbolType.POINT) != null) {
				// TODO namespace = variableName;
				variableName = visitWord(Words.NOT_SERVICE, tokenizer, handler);
			}

			if (variableName != null) {
				tokenizer.commit();

				int dimension = commonDimension + visitDimension(tokenizer, handler);
				if (visitSymbol(tokenizer, handler, SymbolType.EQUATE) != null) {
					if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
						errorOccurred(tokenizer, handler, "expression expected");
					}
				}

				while (visitSymbol(tokenizer, handler, SymbolType.COMMA) != null) {
					if (visitWord(Words.NOT_SERVICE, tokenizer, handler) == null) {
						errorOccurred(tokenizer, handler, "<identifier> is expected");
					}
					dimension = commonDimension + visitDimension(tokenizer, handler);

					if (visitSymbol(tokenizer, handler, SymbolType.EQUATE) != null) {
						if (!ExpressionParseRule.getInstance().visit(tokenizer, handler)) {
							errorOccurred(tokenizer, handler, "expression expected");
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
