package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.NodeLabel;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;
import script.tokenizer.WordToken;
import script.tokenizer.Words;

public class LabelParseRule extends ParseRule<NodeLabel> {
	private final static LabelParseRule instance = new LabelParseRule();

	public static LabelParseRule getInstance() {
		return instance;
	}

	private LabelParseRule() {
	}

	@Override
	public NodeLabel visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		String label;
		if ((label = visitWord(Words.NOT_SERVICE, tokenizer)) != null) {
			if (visitSymbol(tokenizer, Symbols.COLON) != -1) {
				tokenizer.commit();

				Node body = expectBody(tokenizer, properties);

				NodeLabel node = new NodeLabel(label, body);
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}
}
