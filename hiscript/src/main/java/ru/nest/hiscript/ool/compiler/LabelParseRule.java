package ru.nest.hiscript.ool.compiler;

import ru.nest.hiscript.ParseException;
import ru.nest.hiscript.ool.model.Node;
import ru.nest.hiscript.ool.model.nodes.NodeLabel;
import ru.nest.hiscript.tokenizer.Symbols;
import ru.nest.hiscript.tokenizer.Token;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.Words;

public class LabelParseRule extends ParseRule<NodeLabel> {
	private final static LabelParseRule instance = new LabelParseRule();

	public static LabelParseRule getInstance() {
		return instance;
	}

	private LabelParseRule() {
	}

	@Override
	public NodeLabel visit(Tokenizer tokenizer, CompileClassContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		String label;
		if ((label = visitWord(Words.NOT_SERVICE, tokenizer)) != null) {
			Token startToken = tokenizer.currentToken();
			if (visitSymbol(tokenizer, Symbols.COLON) != -1) {
				tokenizer.commit();

				Node body = expectBody(tokenizer, properties);

				NodeLabel node = new NodeLabel(label, body);
				node.setToken(tokenizer.getBlockToken(startToken));
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}
}
