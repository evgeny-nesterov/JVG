package script.pol;

import script.ParseException;
import script.pol.model.BlockNode;
import script.pol.model.Node;
import script.pol.model.Types;
import script.pol.model.Variable;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class RootParseRule extends ParseRule<Node> {
	private final static RootParseRule instance = new RootParseRule();

	public static RootParseRule getInstance() {
		return instance;
	}

	private RootParseRule() {
	}

	public Node visit(Tokenizer tokenizer) throws TokenizerException, ParseException {
		tokenizer.nextToken();
		BlockNode body = BlockParseRule.getInstance().visit(tokenizer);

		skipComments(tokenizer);
		if (tokenizer.hasNext()) {
			throw new ParseException("unexpected token", tokenizer.currentToken());
		}

		if (body != null) {
			initGlobals(body);
		}
		return body;
	}

	public boolean visit(Tokenizer tokenizer, CompileHandler handler) {
		try {
			tokenizer.nextToken();
			BlockParseRule.getInstance().visit(tokenizer, handler);

			skipComments(tokenizer);
			if (tokenizer.hasNext()) {
				errorOccured(tokenizer, handler, "unexpected token");
			}

			return true;
		} catch (TokenizerException exc) {
			errorOccured(tokenizer, handler, exc.getMessage());
		}

		return false;
	}

	private void initGlobals(BlockNode body) {
		try {
			body.addVariable(new Variable("math", "E", Types.DOUBLE, 0)).getValue().setValue(2.7182818284590452354, Types.DOUBLE);
			body.addVariable(new Variable("math", "PI", Types.DOUBLE, 0)).getValue().setValue(3.14159265358979323846, Types.DOUBLE);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
