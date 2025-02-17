package ru.nest.hiscript.ool.compile.parse;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.ool.compile.CompileClassContext;
import ru.nest.hiscript.ool.compile.ParseRule;
import ru.nest.hiscript.ool.model.HiNode;
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
	public NodeLabel visit(Tokenizer tokenizer, CompileClassContext ctx, Token startToken) throws TokenizerException, HiScriptParseException {
		tokenizer.start();

		String label;
		if ((label = visitWord(Words.NOT_SERVICE, tokenizer)) != null) {
			if (visitSymbol(tokenizer, Symbols.COLON) != -1) {
				tokenizer.commit();
				HiNode body = expectBody(tokenizer, ctx);
				return new NodeLabel(label, body);
			}
		}

		tokenizer.rollback();
		return null;
	}
}
