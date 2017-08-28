package script.ool.compiler;

import script.ParseException;
import script.ool.model.Type;
import script.ool.model.nodes.NodeType;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class TypeParseRule extends ParseRule<NodeType> {
	private final static TypeParseRule instance = new TypeParseRule();

	public static TypeParseRule getInstance() {
		return instance;
	}

	private TypeParseRule() {
	}

	public NodeType visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		Type type = visitType(tokenizer, true);
		if (type != null) {
			return new NodeType(type);
		}

		return null;
	}
}
