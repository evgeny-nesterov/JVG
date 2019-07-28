package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.Type;
import script.ool.model.nodes.NodeTry;
import script.tokenizer.SymbolToken;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class TryParseRule extends ParseRule<NodeTry> {
	private final static TryParseRule instance = new TryParseRule();

	public static TryParseRule getInstance() {
		return instance;
	}

	private TryParseRule() {
	}

	@Override
	public NodeTry visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		if (visitWord(Words.TRY, tokenizer) != null) {
			expectSymbol(tokenizer, Symbols.BRACES_LEFT);
			Node tryBody = BlockParseRule.getInstance().visit(tokenizer, properties);
			expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

			// TODO: multiple catch
			Node catchBody = null;
			Type excType = null;
			String excName = null;
			if (visitWord(Words.CATCH, tokenizer) != null) {
				expectSymbol(tokenizer, Symbols.PARANTHESIS_LEFT);
				String typeName = visitWord(NOT_SERVICE, tokenizer);
				if (!"Exception".equals(typeName)) {
					throw new ParseException("Exception is expected", tokenizer.currentToken());
				}
				excType = Type.getType(typeName);
				excName = visitWord(NOT_SERVICE, tokenizer);
				if (excName == null) {
					throw new ParseException("identifier is expected", tokenizer.currentToken());
				}
				expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				catchBody = BlockParseRule.getInstance().visit(tokenizer, properties);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			}

			Node finallyBody = null;
			if (visitWord(Words.FINALLY, tokenizer) != null) {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				finallyBody = BlockParseRule.getInstance().visit(tokenizer, properties);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			}

			NodeTry node = new NodeTry(tryBody, catchBody, excType, excName, finallyBody);
			return node;
		}
		return null;
	}
}
