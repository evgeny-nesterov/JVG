package script.ool.compiler;

import script.ParseException;
import script.ool.model.Node;
import script.ool.model.nodes.NodeInvocation;
import script.tokenizer.Symbols;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class InvocationParseRule extends ParseRule<NodeInvocation> {
	private final static InvocationParseRule instance = new InvocationParseRule();

	public static InvocationParseRule getInstance() {
		return instance;
	}

	private InvocationParseRule() {
	}

	public NodeInvocation visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.start();

		String name = visitWord(tokenizer, NOT_SERVICE);
		if (name != null) {
			if (visitSymbol(tokenizer, Symbols.PARANTHESIS_LEFT) != -1) {
				tokenizer.commit();

				Node[] args = visitArguments(tokenizer, properties);

				expectSymbol(tokenizer, Symbols.PARANTHESIS_RIGHT);

				NodeInvocation node = new NodeInvocation(name, args);
				return node;
			}
		}

		tokenizer.rollback();
		return null;
	}
}
