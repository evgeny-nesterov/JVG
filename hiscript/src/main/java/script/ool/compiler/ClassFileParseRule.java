package script.ool.compiler;

import script.ParseException;
import script.ool.model.Clazz;
import script.ool.model.Node;
import script.tokenizer.Tokenizer;
import script.tokenizer.TokenizerException;

public class ClassFileParseRule extends ParseRule<Node> {
	private final static ClassFileParseRule instance = new ClassFileParseRule();

	public static ClassFileParseRule getInstance() {
		return instance;
	}

	private ClassFileParseRule() {
	}

	public Node visit(Tokenizer tokenizer, CompileContext properties) throws TokenizerException, ParseException {
		tokenizer.nextToken();
		while (true) {
			ClassParseRule.getInstance().skipComments(tokenizer);

			Clazz clazz = ClassParseRule.getInstance().visit(tokenizer, new CompileContext(tokenizer, null, null, Clazz.CLASS_TYPE_TOP));
			if (clazz != null) {
				continue;
			}

			Clazz interfac = InterfaceParseRule.getInstance().visit(tokenizer, new CompileContext(tokenizer, null, null, Clazz.CLASS_TYPE_TOP));
			if (interfac != null) {
				continue;
			}

			break;
		}

		if (tokenizer.hasNext()) {
			throw new ParseException("unexpected token", tokenizer.currentToken());
		}

		return null;
	}
}
