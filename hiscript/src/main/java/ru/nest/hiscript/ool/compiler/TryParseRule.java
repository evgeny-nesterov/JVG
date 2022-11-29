package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.Type;
import ru.nest.hiscript.ool.model.nodes.NodeCatch;
import ru.nest.hiscript.ool.model.nodes.NodeTry;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

import java.util.ArrayList;
import java.util.List;

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

			List<NodeCatch> catchNodes = null;
			while (true) {
				Node catchBody = null;
				List<Type> excTypes = new ArrayList<>(1);
				String excName = null;
				if (visitWord(Words.CATCH, tokenizer) != null) {
					expectSymbol(tokenizer, Symbols.PARENTHESES_LEFT);
					String typeName = visitWord(NOT_SERVICE, tokenizer);
					Type excType = Type.getType(typeName);
					// TODO check excType extends Exception
					excTypes.add(excType);
					while (visitSymbol(tokenizer, Symbols.BITWISE_OR) != -1) {
						typeName = expectWord(NOT_SERVICE, tokenizer);
						excType = Type.getType(typeName);
						// TODO check excType extends Exception
						excTypes.add(excType);
					}
					excName = visitWord(NOT_SERVICE, tokenizer);
					if (excName == null) {
						throw new ParseException("identifier is expected", tokenizer.currentToken());
					}
					expectSymbol(tokenizer, Symbols.PARENTHESES_RIGHT);

					expectSymbol(tokenizer, Symbols.BRACES_LEFT);
					catchBody = BlockParseRule.getInstance().visit(tokenizer, properties);
					expectSymbol(tokenizer, Symbols.BRACES_RIGHT);

					if (catchNodes == null) {
						catchNodes = new ArrayList<>(1);
					}
					catchNodes.add(new NodeCatch(excTypes != null ? excTypes.toArray(new Type[excTypes.size()]) : null, catchBody, excName));
				} else {
					break;
				}
			}

			Node finallyBody = null;
			if (visitWord(Words.FINALLY, tokenizer) != null) {
				expectSymbol(tokenizer, Symbols.BRACES_LEFT);
				finallyBody = BlockParseRule.getInstance().visit(tokenizer, properties);
				expectSymbol(tokenizer, Symbols.BRACES_RIGHT);
			}

			NodeTry node = new NodeTry(tryBody, catchNodes != null ? catchNodes.toArray(new NodeCatch[catchNodes.size()]) : null, finallyBody);
			return node;
		}
		return null;
	}
}
