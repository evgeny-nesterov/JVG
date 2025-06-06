package ru.nest.hiscript.pol;

import ru.nest.hiscript.HiScriptParseException;
import ru.nest.hiscript.pol.model.BlockNode;
import ru.nest.hiscript.pol.model.Node;
import ru.nest.hiscript.pol.model.Variable;
import ru.nest.hiscript.tokenizer.Tokenizer;
import ru.nest.hiscript.tokenizer.TokenizerException;
import ru.nest.hiscript.tokenizer.WordType;

public class RootParseRule extends ParseRule<Node> {
	private final static RootParseRule instance = new RootParseRule();

	public static RootParseRule getInstance() {
		return instance;
	}

	private RootParseRule() {
	}

	@Override
	public Node visit(Tokenizer tokenizer) throws TokenizerException, HiScriptParseException {
		tokenizer.nextToken();
		BlockNode body = BlockParseRule.getInstance().visit(tokenizer);

		if (tokenizer.hasNext()) {
			throw new HiScriptParseException("unexpected token", tokenizer.currentToken());
		}

		if (body != null) {
			initGlobals(body);
		}
		return body;
	}

	@Override
	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		try {
			tokenizer.nextToken();
			BlockParseRule.getInstance().visit(tokenizer, handler);

			if (tokenizer.hasNext()) {
				errorOccurred(tokenizer, handler, "unexpected token");
			}

			return true;
		} catch (TokenizerException exc) {
			errorOccurred(tokenizer, handler, exc.getMessage());
		}
		return false;
	}

	private void initGlobals(BlockNode body) {
		try {
			body.addVariable(new Variable("math", "E", WordType.DOUBLE, 0)).getValue().setValue(2.7182818284590452354, WordType.DOUBLE);
			body.addVariable(new Variable("math", "PI", WordType.DOUBLE, 0)).getValue().setValue(3.14159265358979323846, WordType.DOUBLE);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
