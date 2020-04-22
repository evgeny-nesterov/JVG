package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeTry;
import ru.nest.hiscript.tokenizer.SymbolToken;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordToken;
import ru.nest.hiscript.tokenizer.Words;

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
